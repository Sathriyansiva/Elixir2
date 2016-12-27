package com.invicibledevs.elixir;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.invicibledevs.elixir.dao.ServiceReceiptDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.model.ServiceReceipt;
import com.invicibledevs.elixir.model.User;

import static android.text.TextUtils.isEmpty;

/**
 * Created by admin on 12/23/2016.
 */
public class FeedbackActivity extends AppCompatActivity implements Runnable {
    TextView t1_to,t2_choose,t3_message;
    EditText t4_feedback;
    Button b_save;
    private String[] amountType = {"ADMINISTRATOR", "IT TEAM", "MANAGEMENT"};
    private int amountTypeValue;
    private APIHelpers apiHelper;
    private boolean isFeedbackRequest;
    private ProgressDialog progressDialog;
    private User aUser;
//    private String message2,value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        t1_to=(TextView)findViewById(R.id.to);
        t2_choose=(TextView)findViewById(R.id.choose);
        t3_message=(TextView)findViewById(R.id.message);
        t4_feedback=(EditText)findViewById(R.id.feedback);
        b_save=(Button)findViewById(R.id.save);

    }
    public void choose (View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select The Person");
        builder.setItems(amountType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                t2_choose.setText(amountType[which]);
                amountTypeValue = which + 1;
//                String t3=t2_choose.getText().toString().trim();
//                int  a=new Integer(t3).intValue();
            }
        });
        builder.show();
    }
    public void send (View view) {
        String message2 = t4_feedback.getText().toString().trim();
//        String to = t2_choose.getText().toString().trim();
//        String value = Integer.toString(amountTypeValue).trim();
        if(TextUtils.isEmpty(message2)) {
            t4_feedback.setError("Please Enter Feedback");
            return;
        }
//
//        if(isEmpty(value)) {
//           t2_choose.setError("Please Enter Feedback");
//            return;
//        }

//        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        progressDialog = ProgressDialog.show(FeedbackActivity.this, "", "Please wait...", true);
        progressDialog.dismiss();
        isFeedbackRequest=true;
       Thread thread = new Thread(this);
        thread.start();

    }

    @Override
    public void run()
    {
        String message2 = t4_feedback.getText().toString().trim();
        String value = Integer.toString(amountTypeValue).trim();
        String uid = aUser.getUserId();
        apiHelper = new APIHelpers();
        apiHelper.feedback(message2, value, uid, getApplicationContext(), handler);


    }

    private Handler handler = new Handler() {
        public String responseStatus;
        public String msgTitle;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(FeedbackActivity.this);
                alt_bld.setMessage(responseStatus).setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    } });
                alert.setTitle("Message");
                alert.show();
            }
            if(progressDialog.isShowing())
                progressDialog.dismiss();
            else
            {
                if (msg.what == 0)
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(FeedbackActivity.this);
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
}