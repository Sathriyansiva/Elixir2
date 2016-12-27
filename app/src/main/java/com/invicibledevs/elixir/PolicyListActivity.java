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

import com.invicibledevs.elixir.dao.PolicyDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.model.Policy;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class PolicyListActivity extends AppCompatActivity implements Runnable {

    private PolicyDataSource thePolicyDataSource;
    private ListView listView;
    private TextView noOTWTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private ArrayList<Policy> policyArrayList;
    SharedPreferences elixirPreferences;
    private boolean isGetAddPolicyDetails;
    private User aUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_list);
        listView = (ListView) findViewById(R.id.policy_list);
        noOTWTextView = (TextView) findViewById(R.id.no_policy_label);
        isGetAddPolicyDetails = false;
        if(thePolicyDataSource == null)
            thePolicyDataSource = new PolicyDataSource(getApplicationContext());

        elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);

        policyArrayList = thePolicyDataSource.getAllPolicyByUserId(elixirPreferences.getString("loggedInUserId", ""));
        if(policyArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noOTWTextView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(new CustomAdapter(this, policyArrayList));

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
        policyArrayList = thePolicyDataSource.getAllPolicyByUserId(elixirPreferences.getString("loggedInUserId", ""));
        if(policyArrayList.size() == 0)
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
        customAdapter.setUpdateDataList(policyArrayList);
        customAdapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.scrollBy(0, 0);
    }

    public void showAddPolicyView(View view)
    {
        isGetAddPolicyDetails = true;
        progressDialog = ProgressDialog.show(PolicyListActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void showAddPolicy()
    {
        Intent mainIntent = new Intent(this, AddPolicyActivity.class);
        startActivity(mainIntent);
    }

    public void deleteListItem(View view)
    {
        Policy aPolicy = policyArrayList.get((int)view.getTag());
        thePolicyDataSource.deletePolicyById(aPolicy.getPolicyId());
        reloadListView();;
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<Policy> policyArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(PolicyListActivity policyListActivity, ArrayList<Policy> policyList) {
            policyArrayList =policyList;
            context=policyListActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<Policy> policyList)
        {
            policyArrayList =policyList;
        }
        @Override
        public int getCount() {
            return policyArrayList.size();
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
            TextView customerName, otwId,status;
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

            Policy aPolicy = policyArrayList.get(position);
            holder.customerName.setText("Name: " + aPolicy.getCustomerName());

            if(aPolicy.getPdfPath() == null || aPolicy.getPdfPath().length() == 0) {
                holder.status.setText(aPolicy.getStatus());
                holder.status.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.GONE);
                holder.otwId.setText("Policy Id: " + aPolicy.getPolicyId());
            }
            else {
                holder.status.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.otwId.setText("Policy Id: " + aPolicy.getPolicyId() + "PDF Downloaded");
            }

            return convertView;
        }

    }

    public void refreshPolicy(View view)
    {
        isGetAddPolicyDetails = false;
        progressDialog = ProgressDialog.show(PolicyListActivity.this,
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
            if(isGetAddPolicyDetails)
                apiHelper.getAddPolicyDetails(aUser, getApplicationContext(), handler);
            else
                apiHelper.getPolicyStatus(policyArrayList, getApplicationContext(), handler);
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
            if(isGetAddPolicyDetails)
            {
                if (msg.what == 1) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    showAddPolicy();

                } else if (msg.what == 0) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(PolicyListActivity.this);
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
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(PolicyListActivity.this);
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
