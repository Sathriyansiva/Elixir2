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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.Payment;
import com.invicibledevs.elixir.model.PaymentReport;
import com.invicibledevs.elixir.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PaymentReportActivity extends AppCompatActivity implements Runnable {

    private TextView reportDateTextView, noPaymentTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private User aUser;
    private ListView listView;
    private RadioGroup reportRadioGroup;
    private ArrayList<Payment> paymentArrayList;
    private ArrayList<PaymentReport> paymentReportArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_report);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
        reportRadioGroup = (RadioGroup) findViewById(R.id.reportRadioGroup);
        reportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                progressDialog = ProgressDialog.show(PaymentReportActivity.this,
                        "", "Please wait...", true);
                relodData();
            }

        });
        reportDateTextView = (TextView) findViewById(R.id.textview_report_date);
        noPaymentTextView = (TextView) findViewById(R.id.no_payment_label);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = df.format(c.getTime());
        reportDateTextView.setText(formattedDate);
        progressDialog = ProgressDialog.show(PaymentReportActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
        listView = (ListView) findViewById(R.id.report_list);

        listView.setAdapter(new CustomAdapter(this, new ArrayList<Payment>(), new ArrayList<PaymentReport>()));
    }

    private void relodData()
    {

        Thread thread = new Thread(this);
        thread.start();
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<Payment> paymentArrayList;
        ArrayList<PaymentReport> paymentReportArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(PaymentReportActivity cmsActivity, ArrayList<Payment> paymentLists, ArrayList<PaymentReport> paymentReports) {
            paymentArrayList = paymentLists;
            paymentReportArrayList = paymentReports;
            context=cmsActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<Payment> paymentLists)
        {
            paymentArrayList = paymentLists;
            paymentReportArrayList = null;
        }

        public void setUpdatedReportDataList(ArrayList<PaymentReport> paymentReportLists)
        {
            paymentReportArrayList = paymentReportLists;
            paymentArrayList = null;
        }
        @Override
        public int getCount() {
            if(paymentArrayList != null)
                return paymentArrayList.size();
            else if(paymentReportArrayList != null)
                return paymentReportArrayList.size();
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
            TextView date, cmsAmount, status, otw, totalAmount, paidAmount, differenceAmount;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder;
            if(convertView == null) {
                holder = new Holder();

                convertView = inflater.inflate(R.layout.activity_report_list_item, null);
                holder.date =(TextView) convertView.findViewById(R.id.date);
                holder.cmsAmount = (TextView) convertView.findViewById(R.id.cmsAmount);
                holder.status = (TextView) convertView.findViewById(R.id.status);
                holder.otw = (TextView)convertView.findViewById(R.id.otw);
                holder.totalAmount = (TextView) convertView.findViewById(R.id.totalAmount);
                holder.paidAmount = (TextView) convertView.findViewById(R.id.paidAmount);
                holder.differenceAmount = (TextView)convertView.findViewById(R.id.differeAmount);
                convertView.setTag(holder);
            }
            else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (Holder) convertView.getTag();
            }

            if(paymentArrayList != null) {
                Payment aPayment = paymentArrayList.get(position);
                holder.date.setText("Name: " + aPayment.getName());
                holder.cmsAmount.setText("CMS: " + aPayment.getCms());
                holder.status.setText("Service: " + aPayment.getService());
                holder.otw.setText("OTW: " + aPayment.getOtw());
                holder.totalAmount.setVisibility(View.GONE);
                holder.paidAmount.setVisibility(View.GONE);
                holder.differenceAmount.setVisibility(View.GONE);
            }
            else
            {
                PaymentReport aPaymentReport = paymentReportArrayList.get(position);
                holder.date.setText("Name: " + aPaymentReport.getName());
                holder.cmsAmount.setText("Location: " + aPaymentReport.getLocation() );
                holder.status.setText("Amount Type: " + aPaymentReport.getAmountType());
                holder.otw.setText("Payment Mode: " + aPaymentReport.getPaymentMode());
                holder.totalAmount.setText("Total Amount: " + aPaymentReport.getTotalAmount());
                holder.paidAmount.setText("Paid Amount: " + aPaymentReport.getPaidAmount());
                holder.differenceAmount.setText("Differnce Amount: " + aPaymentReport.getDiffernceAmount());
                holder.totalAmount.setVisibility(View.VISIBLE);
                holder.paidAmount.setVisibility(View.VISIBLE);
                holder.differenceAmount.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

    }

    public void showDatePickerDialog(View v)
    {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aPaymentReportActivity = this;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public PaymentReportActivity aPaymentReportActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = 0;
            int month = 0;
            int day = 0;
            Date riskDate = aPaymentReportActivity.getDateFromString("dd/MM/yyyy", aPaymentReportActivity.getReportDateString());
            if (riskDate != null) {
                final Calendar c = Calendar.getInstance();
                c.setTime(riskDate);
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
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
            if(view.isShown())
                aPaymentReportActivity.showDate(year, month + 1, day);
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
           String strDate = new StringBuilder().append(day).append("/")
                    .append(month).append("/").append(year).toString();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            reportDateTextView.setText(formatter.format(getDateFromString("dd/MM/yyyy", strDate)));
            progressDialog = ProgressDialog.show(PaymentReportActivity.this,
                    "", "Please wait...", true);
            relodData();
        }

        private String getReportDateString()
        {
            return reportDateTextView.getText().toString();
        }

    private void reloadListView() {
        RadioButton selectedRadioButton = (RadioButton)findViewById(reportRadioGroup.getCheckedRadioButtonId());
        CustomAdapter customAdapter = (CustomAdapter) listView.getAdapter();
        if(selectedRadioButton.getId() == R.id.pendingReport) {
            paymentArrayList = Elixir.getPaymentList();
            if (paymentArrayList.size() == 0) {
                listView.setVisibility(View.GONE);
                noPaymentTextView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                noPaymentTextView.setVisibility(View.GONE);
            }
            customAdapter.setUpdateDataList(paymentArrayList);
        }
        else
        {
            paymentReportArrayList = Elixir.getPaymentReportList();
            if (paymentReportArrayList.size() == 0) {
                listView.setVisibility(View.GONE);
                noPaymentTextView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                noPaymentTextView.setVisibility(View.GONE);
            }
            customAdapter.setUpdatedReportDataList(paymentReportArrayList);
        }

        customAdapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.scrollBy(0, 0);
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            apiHelper = new APIHelpers();
            RadioButton selectedRadioButton = (RadioButton)findViewById(reportRadioGroup.getCheckedRadioButtonId());
            apiHelper.getPaymentReportDetails(aUser, getApplicationContext(), handler, reportDateTextView.getText().toString(), selectedRadioButton.getText().toString());
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
            otwId = bundle.getString("otwId");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                reloadListView();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(PaymentReportActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
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
