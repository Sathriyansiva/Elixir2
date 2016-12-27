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
import android.widget.TextView;

import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.ExecutiveReport;
import com.invicibledevs.elixir.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by admin on 11/5/2016.
 */
public class ExecutiveReportActivity extends AppCompatActivity implements Runnable {

    private TextView TDate,FDate,noexeRepTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private User aUser;
    private int year;
    private int month;
    private int day;
    private ListView listView;
    private ArrayList<ExecutiveReport> executiveReportArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executive_report);
        FDate =(TextView) findViewById(R.id.text_from_date);
        TDate =(TextView) findViewById(R.id.text_to_date);
        noexeRepTextView = (TextView) findViewById(R.id.no_exeRep_label);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
        setCurrentDateOnView();
    }

    public void setCurrentDateOnView() {

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = 1;

        FDate.setText(new StringBuilder()
                .append(day).append("/").append(month + 1).append("/")
                .append(year).append(" "));

        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        TDate.setText(new StringBuilder()
                .append(day).append("/").append(month + 1).append("/")
                .append(year).append(" "));
    }

    public void showMonthPickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aAddExeRepActivity = this;
        newFragment.isDateOfReg = false;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showYearPickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aAddExeRepActivity = this;
        newFragment.isDateOfReg = true;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public ExecutiveReportActivity aAddExeRepActivity;
        public boolean isDateOfReg;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int year = 0;
            int month = 0;
            int day = 0;
            Date date = aAddExeRepActivity.getDateFromString("dd/MM/yyyy", aAddExeRepActivity.getRiskDateString());
            if(isDateOfReg)
                date = aAddExeRepActivity.getDateFromString("dd/MM/yyyy", aAddExeRepActivity.getDateOfRegString());
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
            if(isDateOfReg)
                dp.setMaxDate(timeInMills);
            else
//                dp.setMinDate(timeInMills);
                dp.setMaxDate(timeInMills);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if(isDateOfReg)
                aAddExeRepActivity.showDateOfReg(year, month + 1, day);
            else
                aAddExeRepActivity.showRiskStartDate(year, month + 1, day);
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

    private void showRiskStartDate(int year, int month, int day) {

        FDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String getRiskDateString()
    {
        return FDate.getText().toString();
    }

    private void showDateOfReg(int year, int month, int day) {

        TDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String getDateOfRegString()
    {
        return TDate.getText().toString();
    }

    public void savePolicyDetails(View view)
    {

        progressDialog = ProgressDialog.show(ExecutiveReportActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
        listView = (ListView) findViewById(R.id.report_list);

        listView.setAdapter(new CustomAdapter(this,new ArrayList<ExecutiveReport>()));
    }

    public void cancelAddPolicyView(View view)
    {
        finish();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            String frdate = TDate.getText().toString();
            String todate = FDate.getText().toString();
            String uid = aUser.getUserId();
            if(apiHelper == null)
                apiHelper = new APIHelpers();
            apiHelper.sendExecutiveReport(todate,frdate,uid,getApplicationContext(),handler);
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

    public class CustomAdapter extends BaseAdapter {
        ArrayList<ExecutiveReport> executiveReportArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(ExecutiveReportActivity exeActivity, ArrayList<ExecutiveReport> executiveReports) {
            executiveReportArrayList = executiveReports;
            context=exeActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<ExecutiveReport> executiveReportLists)
        {
            executiveReportArrayList = executiveReportLists;
        }
        @Override
        public int getCount() {
            if(executiveReportArrayList != null)
                return executiveReportArrayList.size();
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
            TextView date, intime, outtime, policycount, leadscount, otwcount;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder;
            if(convertView == null) {
                holder = new Holder();

                convertView = inflater.inflate(R.layout.activity_executive_report_list_item, null);
                holder.date =(TextView) convertView.findViewById(R.id.date);
                holder.intime = (TextView) convertView.findViewById(R.id.intime);
                holder.outtime = (TextView) convertView.findViewById(R.id.outtime);
                holder.policycount = (TextView)convertView.findViewById(R.id.policycount);
                holder.leadscount = (TextView) convertView.findViewById(R.id.leadscount);
                holder.otwcount = (TextView) convertView.findViewById(R.id.otwcount);
                convertView.setTag(holder);
            }
            else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (Holder) convertView.getTag();
            }

            if(executiveReportArrayList != null) {
                ExecutiveReport aExecutiveReport = executiveReportArrayList.get(position);
                holder.date.setText("Date: " + aExecutiveReport.getdate());
                holder.intime.setText("In Time: " + aExecutiveReport.getIntime() );
                holder.outtime.setText("Out Time: " + aExecutiveReport.getOuttime());
                holder.policycount.setText("Policy Count: " + aExecutiveReport.getPolicycount());
                holder.leadscount.setText("Leads Count: " + aExecutiveReport.getLeadscount());
                holder.otwcount.setText("OTW Count: " + aExecutiveReport.getOtwcount());
            }

            return convertView;
        }

    }


    private void reloadListView() {
        CustomAdapter customAdapter = (CustomAdapter) listView.getAdapter();
        executiveReportArrayList = Elixir.getExecutiveReportList();
        if (executiveReportArrayList.size() == 0) {
            listView.setVisibility(View.GONE);
            noexeRepTextView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            noexeRepTextView.setVisibility(View.GONE);
        }
        customAdapter.setUpdateDataList(executiveReportArrayList);
        customAdapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.scrollBy(0, 0);
    }

    private Handler handler = new Handler() {
        public String responseStatus;
        public String msgTitle;
        //        public String otwId;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
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
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(ExecutiveReportActivity.this);
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
