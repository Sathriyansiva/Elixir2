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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.CMSListDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.model.CMSList;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class CMSListActivity extends AppCompatActivity implements Runnable {

    private CMSListDataSource theCMSListDataSource;
    private ListView listView;
    private TextView noCMSTextView, cashInHandTextView, amountTypeTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private ArrayList<CMSList> cmsListArrayList;
    private User aUser;
    private String[] amountType = {"CMS", "Service", "Non-CMS"};
    private int amountTypeValue;
    private String cmsAmount, serviceAmount, noncmsAmount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmslist);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));

        listView = (ListView) findViewById(R.id.cms_list);
        noCMSTextView = (TextView) findViewById(R.id.no_cms_label);
        cashInHandTextView = (TextView) findViewById(R.id.textView_cash_in_hand);
        amountTypeTextView = (TextView) findViewById(R.id.text_amount_type);
        cmsListArrayList = new ArrayList<CMSList>();

        if(theCMSListDataSource == null)
            theCMSListDataSource = new CMSListDataSource(getApplicationContext());
        if(cmsListArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noCMSTextView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(new CustomAdapter(this, cmsListArrayList));
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CMSList aCMSList = cmsListArrayList.get(position);
                aCMSList.setIsSelected(!aCMSList.isSelected());
                reloadListView();
            }
        });
        amountTypeTextView.setText(amountType[0]);
        amountTypeValue = 1;
        getCMSList(null);
    }

    public void showAmountTypePickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Amount Type");
        builder.setItems(amountType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                amountTypeTextView.setText(amountType[which]);
                amountTypeValue = which + 1;

                updateListView();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void updateListView()
    {
        switch (amountTypeValue) {
            case 1: {
                cashInHandTextView.setText(cmsAmount);
                cmsListArrayList = theCMSListDataSource.getCMSListByAmountType(APIHelpers.CMSAmountType.CMS.getValue());
            }
            break;
            case 2: {
                cashInHandTextView.setText(serviceAmount);
                cmsListArrayList = theCMSListDataSource.getCMSListByAmountType(APIHelpers.CMSAmountType.SERVICE.getValue());
            }
            break;
            case 3: {
                cashInHandTextView.setText(noncmsAmount);
                cmsListArrayList = theCMSListDataSource.getCMSListByAmountType(APIHelpers.CMSAmountType.NONCMS.getValue());
            }
            break;
        }
        reloadListView();

    }

    private void reloadListView() {
        if(cmsListArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noCMSTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            listView.setVisibility(View.VISIBLE);
            noCMSTextView.setVisibility(View.GONE);
        }
        CustomAdapter customAdapter = (CustomAdapter) listView.getAdapter();
        customAdapter.setUpdateDataList(cmsListArrayList);
        customAdapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.scrollBy(0, 0);
    }


    public class CustomAdapter extends BaseAdapter {
        ArrayList<CMSList> cmsListArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(CMSListActivity cmsListActivity, ArrayList<CMSList> cmsLists) {
            cmsListArrayList =cmsLists;
            context=cmsListActivity;

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
                holder.vehicleno = (TextView) convertView.findViewById(R.id.vehino);
                holder.vehicletype = (TextView) convertView.findViewById(R.id.vehitype);
                holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkbox);
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

            switch (amountTypeValue) {
                case 1: {
                    holder.cmsAmount.setText("CMS Amount: " + aCMSList.getCmsAmount());
                    holder.vehicleno.setVisibility(View.GONE);
                    holder.vehicletype.setVisibility(View.GONE);
                }
                break;
                case 2: {
                    holder.cmsAmount.setText("Service Amount: " + aCMSList.getCmsAmount());
                    holder.vehicleno.setVisibility(View.GONE);
                    holder.vehicletype.setVisibility(View.GONE);
                }
                break;
                case 3: {
                    holder.cmsAmount.setText("Non-CMS Amount: " + aCMSList.getCmsAmount());
                    holder.vehicleno.setText("Vehicle No: " + aCMSList.getCmsVehiNo());
                    holder.vehicletype.setText("Vehicle Type: " + aCMSList.getCmsVehiType());
                    holder.vehicleno.setVisibility(View.VISIBLE);
                    holder.vehicletype.setVisibility(View.VISIBLE);
                }
                break;
            }
//            holder.status.setVisibility(View.GONE);
            holder.checkBox.setChecked(aCMSList.isSelected());
            if(aCMSList.getCmsAmount().equalsIgnoreCase("0") || aCMSList.getCmsAmount().equalsIgnoreCase("0.00"))
            {
                holder.checkBox.setVisibility(View.GONE);
            }
            else
                holder.checkBox.setVisibility(View.VISIBLE);

            return convertView;
        }

    }

    public void getCMSList(View view)
    {
        progressDialog = ProgressDialog.show(CMSListActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            apiHelper = new APIHelpers();
            apiHelper.getCMSList(aUser, getApplicationContext(), handler);
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
            cmsAmount = bundle.getString("totalCMSAmount");
            serviceAmount = bundle.getString("totalServiceAmount");
            noncmsAmount = bundle.getString("totalNonCMSAmount");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                cashInHandTextView.setText(cmsAmount);
                cmsListArrayList = theCMSListDataSource.getCMSListByAmountType(APIHelpers.CMSAmountType.CMS.getValue());
                reloadListView();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(CMSListActivity.this);
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

    public void selectCMSList(View view)
    {
        CMSList aCMSList = cmsListArrayList.get((int) view.getTag());

        aCMSList.setIsSelected(!aCMSList.isSelected());
        reloadListView();
    }

    public void goToCMS(View view)
    {
        ArrayList<CMSList> selectedCMSList = new ArrayList<CMSList>();
        double totalCms = 0;
        for (CMSList aCmsList:cmsListArrayList)
        {
            if(aCmsList.isSelected())
            {
                double cmsValue = Double.parseDouble(aCmsList.getCmsAmount());
                if(cmsValue == 0)
                    continue;
                totalCms = totalCms + Double.parseDouble(aCmsList.getCmsAmount());
                selectedCMSList.add(aCmsList);
            }
        }
        if(selectedCMSList.size() == 0)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(CMSListActivity.this);
            alt_bld.setMessage("Please select "+ amountTypeTextView.getText().toString() + " from list.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        Intent mainIntent = new Intent(this, CMSActivity.class);
        mainIntent.putExtra("CMSList", selectedCMSList);
        mainIntent.putExtra("amountType", amountTypeTextView.getText().toString());
        mainIntent.putExtra("amountTypeValue", amountTypeValue);
        mainIntent.putExtra("TotalCmsAmt", totalCms+"");
        int REQUEST_CODE = 123;
        startActivityForResult(mainIntent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}
