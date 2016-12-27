package com.invicibledevs.elixir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.DecimalDigitsInputFilter;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.CMS;
import com.invicibledevs.elixir.model.CMSList;
import com.invicibledevs.elixir.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CMSActivity extends AppCompatActivity implements Runnable {

    private ListView listView;
    private TextView totalCMSTextView, paidByTextView, dateTextView, nameTextview, amountTypeTextView;
    private EditText paidAmountEditText, transactionIdEditText, remarksEditText, accountNoEditText, otpEditText;
    private LinearLayout cmsCollectorLayout, toBankLayout, cashCollectorLayout;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private CMS theCMS;
    private ArrayList<CMSList> cmsListArrayList;
    private User aUser;
    private String[] paidByList = {"CMS Collector", "To Bank", "Cash Collector", "To Staff"};
    private int paidByValue;
    private String amountTypeValue;
    private JSONArray staffNameArray, cashCollectorNameArray;
    private boolean isOTPRequest;
    private String selectedNameValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms);
        listView = (ListView) findViewById(R.id.cms_list);
        totalCMSTextView = (TextView) findViewById(R.id.text_total_cms);
        paidByTextView = (TextView) findViewById(R.id.text_paid_by);
        dateTextView = (TextView) findViewById(R.id.text_view_date);
        nameTextview = (TextView) findViewById(R.id.text_view_name);
        amountTypeTextView = (TextView) findViewById(R.id.text_amount_type);
        paidAmountEditText = (EditText) findViewById(R.id.editText_paid_amount);
        transactionIdEditText = (EditText) findViewById(R.id.editText_transaction_id);
        remarksEditText = (EditText) findViewById(R.id.editText_remarks);
        accountNoEditText = (EditText) findViewById(R.id.editText_account_no);
        otpEditText = (EditText) findViewById(R.id.editText_otp);
        cmsListArrayList = new ArrayList<CMSList>();
        cmsListArrayList = (ArrayList<CMSList>)getIntent().getSerializableExtra("CMSList");
        totalCMSTextView.setText(getIntent().getStringExtra("TotalCmsAmt"));
        amountTypeTextView.setText(getIntent().getStringExtra("amountType"));
//        Toast.makeText(getApplicationContext(),getIntent().getStringExtra("amountType"),Toast.LENGTH_LONG).show();
        amountTypeValue = getIntent().getStringExtra("amountType");
        cmsCollectorLayout = (LinearLayout) findViewById(R.id.cms_collector_layout);
        cashCollectorLayout = (LinearLayout) findViewById(R.id.cash_collector_layout);
        toBankLayout = (LinearLayout) findViewById(R.id.to_bank_layout);


        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));

        paidAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7,2)});
        listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 300 * cmsListArrayList.size()));

        listView.setAdapter(new CustomAdapter(this, cmsListArrayList));

        paidByTextView.setText(paidByList[0]);
        paidByValue = 1;

        staffNameArray = Elixir.getStaffNameArrayList();
        cashCollectorNameArray = Elixir.getCashCollectorNameJsonArray();
        isOTPRequest = false;
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<CMSList> cmsListArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(CMSActivity cmsActivity, ArrayList<CMSList> cmsLists) {
            cmsListArrayList =cmsLists;
            context=cmsActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<CMSList> cmsLists)
        {
            cmsListArrayList =cmsLists;
        }
        @Override
        public int getCount() {

            return cmsListArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder
        {
            TextView date, cmsAmount,status,vehicleno,vehicletype;
            CheckBox checkBox;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder;
            if(convertView == null) {
                holder = new Holder();

                convertView = inflater.inflate(R.layout.activity_cms_list_item, null);
                holder.date =(TextView) convertView.findViewById(R.id.date);
                holder.cmsAmount = (TextView) convertView.findViewById(R.id.cmsAmount);
//                holder.status = (TextView) convertView.findViewById(R.id.status);
//                holder.status.setVisibility(View.GONE);
                holder.vehicleno = (TextView) convertView.findViewById(R.id.vehino);
                holder.vehicletype = (TextView) convertView.findViewById(R.id.vehitype);
                holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkbox);
                holder.checkBox.setVisibility(View.GONE);
                convertView.setTag(holder);
            }
            else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (Holder) convertView.getTag();
            }
            holder.checkBox.setTag(position);

            CMSList aCMSList = cmsListArrayList.get(position);
            holder.date.setText("Date: " + aCMSList.getCmsDateStr());
            holder.cmsAmount.setText(amountTypeTextView.getText().toString() + ":" + aCMSList.getCmsAmount());
            holder.vehicleno.setVisibility(View.GONE);
            holder.vehicletype.setVisibility(View.GONE);
            String check = amountTypeTextView.getText().toString();
            if(check.equals("Non-CMS"))
            {
                holder.vehicleno.setText("Vehicle No: " + aCMSList.getCmsVehiNo());
                holder.vehicletype.setText("Vehicle Type: " + aCMSList.getCmsVehiType());
                holder.vehicleno.setVisibility(View.VISIBLE);
                holder.vehicletype.setVisibility(View.VISIBLE);
            }
//            if(aCMSList.getStatus() == null)
//                holder.status.setText("Status: ");
//            else
//                holder.status.setText("Status: " + aCMSList.getStatus());
            holder.checkBox.setSelected(aCMSList.isSelected());

            return convertView;
        }

    }

    public void showPaidByPickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Paid By");
        builder.setItems(paidByList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                paidByTextView.setText(paidByList[which]);
                paidByValue = which + 1;
                updateViewByPaidByValue();
            }
        });
        builder.show();
    }

    public void showNamePickerDialog(View v) {

        String paidAmount = paidAmountEditText.getText().toString().trim();

        if (paidAmount.equals("")) {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please enter Paid amount before sending otp.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();

            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alert.setTitle("Error");
            alert.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(paidByValue == 3)
        {
            builder.setTitle("Pick a Cash Collector Name");
            builder.setAdapter(new DialogCustomAdapter(this, cashCollectorNameArray), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        JSONObject jsonObj = cashCollectorNameArray.getJSONObject(which);
                        nameTextview.setText(jsonObj.getString("name"));
                        selectedNameValue = jsonObj.getString("id");
                        sendOTP(null);
                    } catch (JSONException exception) {

                    }
                }
            });
        }
        else {
            builder.setTitle("Pick a Staff Name");
            builder.setAdapter(new DialogCustomAdapter(this, staffNameArray), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        JSONObject jsonObj = staffNameArray.getJSONObject(which);
                        nameTextview.setText(jsonObj.getString("name"));
                        selectedNameValue = jsonObj.getString("id");
                        sendOTP(null);
                    } catch (JSONException exception) {

                    }
                }
            });
        }
        builder.show();
    }

    public void sendOTP(View v) {
        isOTPRequest = true;
        progressDialog = ProgressDialog.show(CMSActivity.this,
                "", "Sending OTP...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public class DialogCustomAdapter extends BaseAdapter {
        JSONArray jsonCustomArray;
        Context context;

        private LayoutInflater inflater=null;
        public DialogCustomAdapter(CMSActivity otwActivity, JSONArray jsonArray) {
            jsonCustomArray = jsonArray;
            context=otwActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(JSONArray jsonArray)
        {
            jsonCustomArray =jsonArray;
        }
        @Override
        public int getCount() {
            if(jsonCustomArray != null)
                return jsonCustomArray.length();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder
        {
            TextView titleTextView;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder;
            if(convertView == null) {
                holder = new Holder();

                convertView = inflater.inflate(R.layout.list_row, null);
                holder.titleTextView =(TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            }
            else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (Holder) convertView.getTag();
            }

            try {
                JSONObject aJSONObj = jsonCustomArray.getJSONObject(position);

                if(aJSONObj.has("name"))
                    holder.titleTextView.setText(aJSONObj.getString("name"));

            }
            catch (JSONException exception)
            {

            }
            return convertView;
        }

    }

    private void updateViewByPaidByValue()
    {
        switch (paidByValue)
        {
            case 1:
            {
                cmsCollectorLayout.setVisibility(View.VISIBLE);
                cashCollectorLayout.setVisibility(View.GONE);
                toBankLayout.setVisibility(View.GONE);
                transactionIdEditText.setText("");
            }
                break;
            case 2:
            {
                cmsCollectorLayout.setVisibility(View.GONE);
                cashCollectorLayout.setVisibility(View.GONE);
                toBankLayout.setVisibility(View.VISIBLE);
                dateTextView.setText("");
                accountNoEditText.setText("");
            }
                break;
            case 3:
            {
                cmsCollectorLayout.setVisibility(View.GONE);
                cashCollectorLayout.setVisibility(View.VISIBLE);
                toBankLayout.setVisibility(View.GONE);
                otpEditText.setText("");
                nameTextview.setText("");
            }
                break;
            case 4:
            {
                cmsCollectorLayout.setVisibility(View.GONE);
                cashCollectorLayout.setVisibility(View.VISIBLE);
                toBankLayout.setVisibility(View.GONE);
                otpEditText.setText("");
                nameTextview.setText("");
            }
                break;
            default:
                break;

        }
    }

    public void saveCMSDetails(View view)
    {

        String totalCMS = totalCMSTextView.getText().toString().trim();
        String paidAmount = paidAmountEditText.getText().toString().trim();
        String transactionId = transactionIdEditText.getText().toString().trim();

        String paidBy = paidByTextView.getText().toString().trim();
        String paidDate = dateTextView.getText().toString().trim();
        String accNo = accountNoEditText.getText().toString().trim();
        String name = nameTextview.getText().toString().trim();
        String amountType = amountTypeTextView.getText().toString().trim();
        String otp = otpEditText.getText().toString().trim();

        String remarks = remarksEditText.getText().toString().trim();

        if(paidByValue == 1) {

            if (paidAmount.equals("") || transactionId.equals("")) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
                alt_bld.setMessage("All fields are required.")
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();

                alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.setTitle("Error");
                alert.show();
                return;
            }
        }
        else if(paidByValue == 2) {

            if (paidAmount.equals("") || paidDate.equals("") || accNo.equals("")) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
                alt_bld.setMessage("All fields are required.")
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();

                alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.setTitle("Error");
                alert.show();
                return;
            }
        }
        else{

            if (paidAmount.equals("") || name.equals("") || otp.equals("")) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
                alt_bld.setMessage("All fields are required.")
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();

                alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.setTitle("Error");
                alert.show();
                return;
            }
        }
        double diff = Double.parseDouble(totalCMS) - Double.parseDouble(paidAmount);
        CMS aCMS = new CMS();
        aCMS.setTotalCMSAmount(Double.parseDouble(totalCMS));
        aCMS.setPaidAmount(Double.parseDouble(paidAmount));
        aCMS.setTransactionId(transactionId);
        aCMS.setRemarks(remarksEditText.getText().toString());
        aCMS.setCmsLists(cmsListArrayList);
        aCMS.setPaidBy(paidBy);
        aCMS.setPaidDate(paidDate);
        aCMS.setPaidAccNo(accNo);
        if(paidByValue == 3) {
            aCMS.setCashcollName(selectedNameValue);
            aCMS.setStaffName("");
        }
        else {
            aCMS.setCashcollName("");
            if(selectedNameValue != null)
                aCMS.setStaffName(selectedNameValue);
            else
                aCMS.setStaffName("");
        }
        aCMS.setPaymentMode(amountType);
        aCMS.setOtp(otp);

        theCMS = aCMS;
        if(diff != 0 )
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Differnce amount between Total amount and Paid amount is "+Math.abs(diff)+". Do you want to save?")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveCMS();
                }
            });
            alert.setButton(-2, "NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alert.setTitle("Error");
            alert.show();

        }
        else
            saveCMS();

    }

    private void saveCMS()
    {
        isOTPRequest = false;
        progressDialog = ProgressDialog.show(CMSActivity.this,
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
            if(isOTPRequest)
                apiHelper.sendOTP(selectedNameValue, paidAmountEditText.getText().toString().trim(), aUser, getApplicationContext(), handler);
            else
                apiHelper.sendCMS(theCMS, amountTypeValue, aUser, getApplicationContext(), handler);
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
        public String otwId;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

                if(isOTPRequest)
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(CMSActivity.this);
                    alt_bld.setMessage("OTP sent Successfully. Please enter the OTP.")
                            .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        } });
                    alert.setTitle(msgTitle);
                    alert.show();
                }
                else
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(CMSActivity.this);
                    alt_bld.setMessage(responseStatus)
                            .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setResultOkSoSecondActivityWontBeShown();
                            finish();
                        } });
                    alert.setTitle(msgTitle);
                    alert.show();
                }


                if(progressDialog.isShowing())
                    progressDialog.dismiss();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(CMSActivity.this);
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

    private void setResultOkSoSecondActivityWontBeShown() {
        Intent intent = new Intent();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
    }

    public void cancelCMSView(View view)
    {
        finish();
    }

    public void showDatePickerDialog(View v)
    {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aCMSActivity = this;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public CMSActivity aCMSActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = 0;
            int month = 0;
            int day = 0;
            Date date = aCMSActivity.getDateFromString("dd/MM/yyyy", aCMSActivity.getDateString());
            if(date != null) {
                final Calendar c = Calendar.getInstance();
                c.setTime(date);
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
            dp.setMaxDate(timeInMills);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            aCMSActivity.showDate(year, month + 1, day);
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

    private String getDateString()
    {
        return dateTextView.getText().toString();
    }

    private void showDate(int year, int month, int day) {

        dateTextView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
}
