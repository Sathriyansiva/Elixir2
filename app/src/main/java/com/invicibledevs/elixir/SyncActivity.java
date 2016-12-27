package com.invicibledevs.elixir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.ServiceReceiptDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.model.ServiceReceipt;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class SyncActivity extends AppCompatActivity implements Runnable{

    private TextView pendingStatusTextView;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;
    private User aUser;
    private ServiceReceiptDataSource theServiceReceiptDataSource;
    private ArrayList<ServiceReceipt> theServiceReceipts;
    private ServiceReceipt theServiceReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        pendingStatusTextView = (TextView)findViewById(R.id.pendingStatus);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
        theServiceReceiptDataSource = new ServiceReceiptDataSource(getApplicationContext());
        theServiceReceipts = theServiceReceiptDataSource.getAllServiceReceipts();
        pendingStatusTextView.setText("You have " + theServiceReceipts.size() + " receipts images which needs to sync.");

    }

    public void syncServiceReceiptImages(View view)
    {
        if(theServiceReceipts.size() > 0)
        {
            saveServiceReceipt();
        }
    }

    private void saveServiceReceipt()
    {

        theServiceReceipt = theServiceReceipts.get(0);
        progressDialog = ProgressDialog.show(SyncActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }
    public void cancelServiceReceiptView(View view)
    {
        finish();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            apiHelper = new APIHelpers();
            apiHelper.sendServiceReceiptImages(theServiceReceipt, aUser, getApplicationContext(), handler);
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
        public String msgTitle, logError;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            logError = bundle.getString("logError");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();


                theServiceReceiptDataSource.deleteServiceReceiptById(theServiceReceipt.getId()+"");
                theServiceReceipts = theServiceReceiptDataSource.getAllServiceReceipts();
                pendingStatusTextView.setText("You have " + theServiceReceipts.size() + " receipts images which needs to sync.");
                if(theServiceReceipts.size() == 0)
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(SyncActivity.this);
                    alt_bld.setMessage("Sync completed Successfully.")
                                .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    alert.setTitle(msgTitle);
                    alert.show();
                }
                else
                {
                    saveServiceReceipt();
                }


            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(SyncActivity.this);
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
}
