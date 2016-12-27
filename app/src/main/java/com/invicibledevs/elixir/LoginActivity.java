package com.invicibledevs.elixir;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.LocationService;


public class LoginActivity extends AppCompatActivity implements Runnable {

    private TextView imeiTextView, versionLabel;
    private EditText userNameTextField, passwordTextField;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private boolean isIMEIRequest;
    private Location currentLocation;
    private String appVersion;
    static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        imeiTextView = (TextView) findViewById(R.id.text_imei);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        }
        else {
            TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = mngr.getDeviceId();
            imeiTextView.setText(imei);
        }
        userNameTextField = (EditText) findViewById(R.id.editText_username);
        passwordTextField = (EditText) findViewById(R.id.editText_password);
        versionLabel = (TextView)findViewById(R.id.versionLabel);
        PackageInfo pinfo;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionLabel.setText("V"+pinfo.versionName);
            appVersion = pinfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            versionLabel.setText("");
        }
    }

    public void sendIMEINumber(View view)
    {
        LocationService aLocationService = LocationService.getLocationManager(getApplicationContext());
//        aLocationService.stopUpdates(getApplicationContext());
        aLocationService.initLocationService(getApplicationContext());
        currentLocation = aLocationService.location;
        if(currentLocation == null)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(LoginActivity.this);
            alt_bld.setMessage("Please enable GPS service in settings to send key")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        String imeiNumber = imeiTextView.getText().toString().trim();
        if(imeiNumber.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("IMEI number is empty.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
        }
        else
        {
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "", "Sending IMEI number...", true);
            isIMEIRequest = true;
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    public void authenticateUser(View view)
    {
//        Intent mainIntent = new Intent(this, CaptureSignatureActivity.class);
//        startActivity(mainIntent);

        String username = userNameTextField.getText().toString().trim();
        String password = passwordTextField.getText().toString();
        if(username.equals("") || password.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Username and Password are required.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
        }
        else
        {

            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "", "Please wait...", true);
            isIMEIRequest = false;
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            if(isIMEIRequest)
            {
                String imei = imeiTextView.getText().toString().trim();

                apiHelper = new APIHelpers();
                apiHelper.sendIMEINumber(imei, getApplicationContext(), handler, currentLocation);
            }
            else {
                String username = userNameTextField.getText().toString().trim();
                String password = passwordTextField.getText().toString();
//                String imei = "865980024845775";
                String imei = imeiTextView.getText().toString().trim();
//                String imei = "356273074427405";
//                String imei = "356899051195283";
                apiHelper = new APIHelpers();
                apiHelper.authenticateUser(username, password, imei, getApplicationContext(), handler);
            }
        }
        else
        {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    private Handler handler = new Handler() {
        public String responseStatus;
        public String msgTitle;
        public String appLatestVersion;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            appLatestVersion = bundle.getString("appversion");
            if(msg.what == 1)
            {
                if(isIMEIRequest)
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(LoginActivity.this);
                    alt_bld.setMessage("Key sent Successfully")
                            .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        } });
                    alert.setTitle("Message");
                    alert.show();
                }
                else {
                    if(appLatestVersion.equalsIgnoreCase(appVersion)) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        AlertDialog.Builder alt_bld = new AlertDialog.Builder(LoginActivity.this);
                        alt_bld.setMessage("You are using old version app. Please use latest version "+appLatestVersion+".")
                                .setCancelable(true);
                        AlertDialog alert = alt_bld.create();
                        alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            } });
                        alert.setTitle("Error");
                        alert.show();
                    }
                }
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(LoginActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    } });
                alert.setTitle(msgTitle);
                alert.show();
            }

        }
    };

    /**
     * Returns the internet connection status
     * @return
     */
    private  boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni!=null && ni.isAvailable() && ni.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
        if(progressDialog != null && !progressDialog.isShowing())
        {
            userNameTextField.setText("");
            passwordTextField.setText("");
            userNameTextField.setFocusable(true);
            passwordTextField.requestFocus();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.cancel();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String imei = mngr.getDeviceId();
                    imeiTextView.setText(imei);

                } else {
                    imeiTextView.setText("Request Denied.");
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
