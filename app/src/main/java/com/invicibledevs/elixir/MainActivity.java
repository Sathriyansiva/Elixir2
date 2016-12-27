package com.invicibledevs.elixir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.ServiceReceiptDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.LocationService;
import com.invicibledevs.elixir.model.ServiceReceipt;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Runnable{

    private TextView marqueeGNews, marqueePNews, welcomeMsgTextView, locationMsgTextView, versionLabel, marqueeDetails ,feedback;
    private UserDataSource theUserDataSource;
    private LocationManager mLocationManager;
    private Location currentLocation;
    private LinearLayout msgLinearLayout, regularUserDashboardLayout, agentDashBoardLayout;
    private RelativeLayout reportDashboardLayout;
    ServiceReceiptDataSource theServiceReceiptDataSource;
    private User aUser;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeMsgTextView = (TextView) findViewById(R.id.welcome_msg);
        marqueeGNews = (TextView)findViewById(R.id.marquee_g_news);
        marqueeGNews.setSelected(true);
        marqueePNews = (TextView)findViewById(R.id.marquee_p_news);
        marqueePNews.setSelected(true);
        marqueeDetails = (TextView)findViewById(R.id.marquee_details);
        marqueeDetails.setSelected(true);
        locationMsgTextView = (TextView)findViewById(R.id.location);
        versionLabel = (TextView)findViewById(R.id.versionLabel);
        msgLinearLayout = (LinearLayout) findViewById(R.id.msgLayout);
        regularUserDashboardLayout = (LinearLayout) findViewById(R.id.regularUserDashboardLayout);
        reportDashboardLayout = (RelativeLayout) findViewById(R.id.reportUserDashboardLayout);
        agentDashBoardLayout = (LinearLayout) findViewById(R.id.agentUserDashboardLayout);

        if(theUserDataSource == null)
            theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);

        String roleName = elixirPreferences.getString("rolename", "");
        if(roleName.equalsIgnoreCase("Bc Collector"))
        {
            msgLinearLayout.setVisibility(View.GONE);
            regularUserDashboardLayout.setVisibility(View.GONE);
            locationMsgTextView.setVisibility(View.GONE);
            reportDashboardLayout.setVisibility(View.VISIBLE);
            agentDashBoardLayout.setVisibility(View.GONE);
        }
        else if(roleName.equalsIgnoreCase("Agent"))
        {
            msgLinearLayout.setVisibility(View.VISIBLE);
            regularUserDashboardLayout.setVisibility(View.GONE);
            locationMsgTextView.setVisibility(View.GONE);
            reportDashboardLayout.setVisibility(View.GONE);
            agentDashBoardLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            msgLinearLayout.setVisibility(View.VISIBLE);
            regularUserDashboardLayout.setVisibility(View.VISIBLE);
            locationMsgTextView.setVisibility(View.VISIBLE);
            reportDashboardLayout.setVisibility(View.GONE);
            agentDashBoardLayout.setVisibility(View.GONE);
        }

        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId",""));
        welcomeMsgTextView.setText("Welcome " + aUser.getExecutiveName() + "!!!");
        locationMsgTextView.setText( elixirPreferences.getString("areaName",""));
        marqueeGNews.setText(aUser.getGeneralNews());
        marqueePNews.setText(aUser.getPersonalNews());
        marqueeDetails.setText(aUser.getDetails());

        PackageInfo pinfo;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionLabel.setText("V"+pinfo.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            versionLabel.setText("");
        }


    }

    public void showAttendanceView(View view)
    {
        if(theServiceReceiptDataSource == null)
            theServiceReceiptDataSource = new ServiceReceiptDataSource(getApplicationContext());


        ArrayList<ServiceReceipt> theServiceReceipts = theServiceReceiptDataSource.getAllServiceReceipts();
        if(theServiceReceipts.size()>20)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
            alt_bld.setMessage("You have saved more than 80 Service Receipt images locally. Please Sync those images first and then do attendance.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        LocationService aLocationService = LocationService.getLocationManager(getApplicationContext());
        aLocationService.initLocationService(getApplicationContext());
        Location currentLocation = aLocationService.location;
        float[] results = new float[1];
        if(currentLocation == null)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
            alt_bld.setMessage("Please enable GPS service in settings to do attendance")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        else if(aUser.getLatitude() == null || aUser.getLongitude() == null || aUser.getLatitude().length() == 0 || aUser.getLongitude().length() == 0)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
            alt_bld.setMessage("Please contact admin since your're not mapped to location.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        Location.distanceBetween(Double.parseDouble(aUser.getLatitude()), Double.parseDouble(aUser.getLongitude()), currentLocation.getLatitude(), currentLocation.getLongitude(), results);
//        Location.distanceBetween(Double.parseDouble(aUser.getLatitude()), Double.parseDouble(aUser.getLongitude()), 13.088618, 80.284861, results);

        if(results[0]<400) {

            Intent mainIntent = new Intent(this, AttendanceActivity.class);
            startActivity(mainIntent);
        }
        else
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);

            String distance = "Distance:"+results[0];
            String workLocation = "Work Location:" + aUser.getLatitude() +"," + aUser.getLongitude();
            String currentLocationStr = " Current Location:" + currentLocation.getLatitude() +"," + currentLocation.getLongitude();
            alt_bld.setMessage("Please visit the work location to do attendance. "+workLocation+currentLocationStr +" "+distance )
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
        }
    }

    public void showServiceReceiptView(View view)
    {
        getServiceReceiptDetails();
    }

    public void showOTWView(View view)
    {
        Intent mainIntent = new Intent(this, OTWListActivity.class);
        startActivity(mainIntent);
    }

    public void showLeadsView(View view)
    {
        Intent mainIntent = new Intent(this, LeadsActivity.class);
        startActivity(mainIntent);
    }

    public void showCMSView(View view)
    {
        Intent mainIntent = new Intent(this, CMSListActivity.class);
        startActivity(mainIntent);
    }

    public void showEntryView(View view)
    {
        Intent mainIntent = new Intent(this, PaymentEntryActivity.class);
        startActivity(mainIntent);
    }

    public void showReportView(View view)
    {
        Intent mainIntent = new Intent(this, PaymentReportActivity.class);
        startActivity(mainIntent);
    }

    public void showPoliciesView(View view)
    {
        Intent mainIntent = new Intent(this, PolicyListActivity.class);
        startActivity(mainIntent);
    }

    public void showPaymentEntryView(View view)
    {
        Intent mainIntent = new Intent(this, AgentPaymentEntryActivity.class);
        startActivity(mainIntent);
    }

    public void showSendPhotosView(View view)
    {
        Intent mainIntent = new Intent(this, PhotosPolicyActivity.class);
        startActivity(mainIntent);
    }

    public void showReceiveFilesView(View view)
    {
        Intent mainIntent = new Intent(this, ReceiveFilesActivity.class);
        startActivity(mainIntent);
    }
    public void sendFeedback(View view){
        Intent mainIntent=new Intent(this,FeedbackActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
           logoutApp(null);
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void logoutApp(View view)
    {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("Do you want to Logout?")
                .setCancelable(true);
        AlertDialog alert = alt_bld.create();
        alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                theUserDataSource.deleteUserByUserId(aUser.getUserId());
                finish();
            }
        });
        alert.setButton(-2, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setTitle("Confirm");
        alert.show();
    }

    public void getServiceReceiptDetails()
    {
        progressDialog = ProgressDialog.show(MainActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            if(apiHelper == null)
                apiHelper = new APIHelpers();
            apiHelper.getServiceReceiptDetails(aUser, getApplicationContext(), handler);
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
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            if (msg.what == 1)
            {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                showServiceReceipt();
            }
            else if (msg.what == 0)
            {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
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

    private void showServiceReceipt()
    {
        Intent mainIntent = new Intent(this, ServiceReceiptActivity.class);
        startActivity(mainIntent);
    }

    public void showExecutiveReportView(View view)
    {
        Intent mainIntent = new Intent(this, ExecutiveReportActivity.class);
        startActivity(mainIntent);
    }
}
