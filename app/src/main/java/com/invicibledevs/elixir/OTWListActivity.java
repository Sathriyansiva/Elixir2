package com.invicibledevs.elixir;

import android.app.AlertDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.OTWDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.model.OTW;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class OTWListActivity extends AppCompatActivity implements Runnable {

    private OTWDataSource theOTWDataSource;
    private ListView listView;
    private TextView noOTWTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private ArrayList<OTW> otwArrayList;
    private boolean isGetOTWDetails;
    private User aUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otwlist);
        listView = (ListView) findViewById(R.id.otw_list);
        noOTWTextView = (TextView) findViewById(R.id.no_otw_label);
        isGetOTWDetails = false;
        if(theOTWDataSource == null)
            theOTWDataSource = new OTWDataSource(getApplicationContext());
        otwArrayList = theOTWDataSource.getAllOTW();
        if(otwArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noOTWTextView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(new CustomAdapter(this, otwArrayList));
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadListView();

    }

    private void reloadListView() {
        otwArrayList = theOTWDataSource.getAllOTW();
        if(otwArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noOTWTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            listView.setVisibility(View.VISIBLE);
            noOTWTextView.setVisibility(View.GONE);
        }
        CustomAdapter customAdapter = (CustomAdapter) listView.getAdapter();
        customAdapter.setUpdateDataList(otwArrayList);
        customAdapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.scrollBy(0, 0);
    }

    public void addOtw(View view)
    {
        isGetOTWDetails = true;
        progressDialog = ProgressDialog.show(OTWListActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void showAddOTW()
    {
        Intent mainIntent = new Intent(this, OTWActivity.class);
        startActivity(mainIntent);
    }

    public void deleteListItem(View view)
    {
        OTW aOtw = otwArrayList.get((int)view.getTag());
        theOTWDataSource.deleteOTWById(aOtw.getOtwId());
        reloadListView();;
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<OTW> otwArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(OTWListActivity otwListActivity, ArrayList<OTW> otwList) {
            otwArrayList=otwList;
            context=otwListActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<OTW> otwList)
        {
            otwArrayList=otwList;
        }
        @Override
        public int getCount() {
            return otwArrayList.size();
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
            TextView customerName, otwId,status, vehcileNo;
            Button deleteButton;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder;
            if(convertView == null) {
                holder = new Holder();

                convertView = inflater.inflate(R.layout.activity_listview, null);
                holder.customerName =(TextView) convertView.findViewById(R.id.label);
                holder.otwId = (TextView) convertView.findViewById(R.id.otID);
                holder.vehcileNo = (TextView) convertView.findViewById(R.id.vehicle_no);
                holder.status = (TextView) convertView.findViewById(R.id.status);
                holder.deleteButton = (Button)convertView.findViewById(R.id.btn_delete);
                convertView.setTag(holder);
            }
            else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (Holder) convertView.getTag();
            }
            holder.deleteButton.setTag(position);

            OTW aOTW = otwArrayList.get(position);
            holder.customerName.setText("Name: "+aOTW.getCustomerName());
            holder.vehcileNo.setVisibility(View.VISIBLE);
            holder.vehcileNo.setText("Vehicle No: "+aOTW.getVehicleNo());

            if(aOTW.getPdfPath() == null || aOTW.getPdfPath().length() == 0) {
                holder.status.setText(aOTW.getStatus());
                holder.status.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.GONE);
                holder.otwId.setText("Trans Id: " + aOTW.getOtwId());
            }
            else {
                holder.status.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.otwId.setText("Trans Id: " + aOTW.getOtwId() + "PDF Downloaded");
            }

            return convertView;
        }

    }

    public void refreshOtw(View view)
    {
        isGetOTWDetails = false;
        progressDialog = ProgressDialog.show(OTWListActivity.this,
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
            if(isGetOTWDetails)
                apiHelper.getOTWDetails(aUser, getApplicationContext(), handler);
            else
                apiHelper.getOTWStatus(otwArrayList, getApplicationContext(), handler);
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
            if(isGetOTWDetails)
            {
                if (msg.what == 1) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    showAddOTW();

                } else if (msg.what == 0) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(OTWListActivity.this);
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
            else {
                otwId = bundle.getString("otwId");
                if (msg.what == 1) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    reloadListView();

                } else if (msg.what == 0) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(OTWListActivity.this);
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
