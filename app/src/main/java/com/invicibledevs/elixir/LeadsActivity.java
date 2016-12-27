package com.invicibledevs.elixir;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.DecimalDigitsInputFilter;
import com.invicibledevs.elixir.model.Leads;
import com.invicibledevs.elixir.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LeadsActivity extends AppCompatActivity implements Runnable {

    private TextView vehicleTypeTextView, riskEndDateTextView, ncbTextView;
    private EditText previousInsuranceEditText, nameEditText, mobileNoEditText, vehicleNoEditText, idvEditText, emailEditText, yomEditText, makeEditText, modelEditText;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;
    private Leads theLead;
    private User aUser;
    private String[] vehicleType = {"Two Wheeler", "Three Wheeler", "PCCV", "GCCV", "Private Car"};
    private String[] ncb = {"0%", "20%", "25%", "35%", "45%", "50%"};
    private int vehicleTypeValue, ncbValue=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leads);

        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));

        vehicleTypeTextView = (TextView) findViewById(R.id.text_vehicle_type);
        riskEndDateTextView = (TextView)findViewById(R.id.text_view_risk_end_date);
        ncbTextView = (TextView) findViewById(R.id.text_view_ncb);

        previousInsuranceEditText = (EditText)findViewById(R.id.editText_previous_insurance);
        nameEditText = (EditText) findViewById(R.id.editText_name);
        mobileNoEditText = (EditText) findViewById(R.id.editText_mobile_no);
        vehicleNoEditText = (EditText) findViewById(R.id.editText_vehicle_no);
        idvEditText = (EditText) findViewById(R.id.editText_idv);
        emailEditText = (EditText) findViewById(R.id.editText_e_mail);
        yomEditText = (EditText) findViewById(R.id.editText_year_of_manufacture);
        makeEditText = (EditText) findViewById(R.id.editText_make);
        modelEditText = (EditText) findViewById(R.id.editText_model);

        idvEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8,2)});
    }

    public void showVehicleTypePickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Vehicle Type");
        builder.setItems(vehicleType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                vehicleTypeTextView.setText(vehicleType[which]);
                vehicleTypeValue = which+1;
            }
        });
        builder.show();
    }

    public void showNCBDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a NCB");
        builder.setItems(ncb, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ncbTextView.setText(ncb[which]);
                ncbValue = which+1;
            }
        });
        builder.show();
    }

    public void showDatePickerDialog(View v)
    {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aLeadsActivity = this;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public LeadsActivity aLeadsActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = 0;
            int month = 0;
            int day = 0;
            Date riskDate = aLeadsActivity.getDateFromString("dd/MM/yyyy", aLeadsActivity.getRiskDateString());
            if(riskDate != null) {
                final Calendar c = Calendar.getInstance();
                c.setTime(riskDate);
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }
            else
            {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            DatePicker dp = datePickerDialog.getDatePicker();
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            long timeInMills = c.getTimeInMillis();
            dp.setMinDate(timeInMills);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            aLeadsActivity.showDate(year, month + 1, day);
        }


    }

    private Date getDateFromString(String formate, String dateString)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat(formate);
            Date date = formatter.parse(dateString);
            return date;
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    private void showDate(int year, int month, int day) {
        riskEndDateTextView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String getRiskDateString()
    {
        return riskEndDateTextView.getText().toString();
    }

    public void saveLeadsDetails(View view)
    {
        String name = nameEditText.getText().toString().trim();
        String mobileNo = mobileNoEditText.getText().toString().trim();
        String vehicleNo = vehicleNoEditText.getText().toString().trim();
        String idv = idvEditText.getText().toString().trim();
        String vehicleType = vehicleTypeTextView.getText().toString().trim();
        String previousInsurance = previousInsuranceEditText.getText().toString().trim();
        String yom = yomEditText.getText().toString().trim();
        String make = makeEditText.getText().toString().trim();
        String model = modelEditText.getText().toString().trim();

        String ncb = ncbTextView.getText().toString().trim();

        int letcount = 0,nocount = 0;
        for( int i = 0; i < vehicleNo.length( ); i++ )
        {
            char temp = vehicleNo.charAt( i );
            if(Character.isDigit(temp)) {
                nocount++;
            }
            else
            {
                letcount++;
            }
        }
        if(name.equals("") || mobileNo.equals("") || vehicleNo.equals("") || vehicleType.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Name, Vehicle Type, Mobile No and Vehicle No are required.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        else if(mobileNo.length() <10)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Mobile number should be 10 digit.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        else if((letcount <= 2) || (nocount <= 2) || (vehicleNo.length() < 6))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please enter valid vehicle no.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }

        Leads aLead = new Leads();
        aLead.setName(name);
        aLead.setVehicleType(vehicleTypeValue + "");
        aLead.setVehicleNo(vehicleNo);
        if(idv.equals(""))
            aLead.setIdv(0);
        else
            aLead.setIdv(Double.parseDouble(idv));
        aLead.setPreviousInsurance(previousInsurance);
        aLead.setMobileNo(mobileNo);

        Date riskEndDate = getDateFromString("dd/MM/yyyy", riskEndDateTextView.getText().toString());
        if(riskEndDate != null) {
            aLead.setRiskEndDateString(riskEndDateTextView.getText().toString());
            aLead.setRiskEndDate(riskEndDate);
        }
        else
        {
            aLead.setRiskEndDateString("");
            aLead.setRiskEndDate(null);
        }
        if(ncbValue != -1)
            aLead.setNcb(ncbValue + "");
        else
            aLead.setNcb("");
        aLead.setEmail(emailEditText.getText().toString());
        aLead.setYom(yom);
        aLead.setMake(make);
        aLead.setModel(model);

        theLead = aLead;

        progressDialog = ProgressDialog.show(LeadsActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void cancelLeadsView(View view)
    {
        finish();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            apiHelper = new APIHelpers();
            apiHelper.sendLead(theLead, aUser, getApplicationContext(), handler);
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
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(LeadsActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    } });
                alert.setTitle(msgTitle);
                alert.show();

                if(progressDialog.isShowing())
                    progressDialog.dismiss();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(LeadsActivity.this);
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
