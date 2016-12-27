package com.invicibledevs.elixir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.DecimalDigitsInputFilter;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.Payment;
import com.invicibledevs.elixir.model.PaymentEntry;
import com.invicibledevs.elixir.model.PaymentTypeDetails;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class PaymentEntryActivity extends AppCompatActivity implements Runnable {

    private User aUser;
    private EditText chequeAmountEditText, cashAmountEditText, remarksEditText;
    private TextView executiveNameTextView, cmsAmountTextView, serviceChargeTextView, otwTextView, totalTextView, paymentTextView, noDataTextView, selectedItemsTotalTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private int paymentTypeValue = -1, executiveNameValue;
    private String[] paymentType = {"Service Charge", "CMS", "OTW"};
    private ArrayList<String> executiveNames;
    private ArrayList<PaymentTypeDetails> paymentTypeDetailsArrayList;
    private Payment selectedPayment;
    private ListView listView;
    private boolean isSendRequest;
    private PaymentEntry aPaymentEntry;
    private float pixelHeight;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_entry);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
        chequeAmountEditText = (EditText) findViewById(R.id.text_cheque_amount);
        cashAmountEditText = (EditText) findViewById(R.id.editText_cash_amount);
        remarksEditText = (EditText) findViewById(R.id.editText_remarks);
        cashAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(9,2)});
        chequeAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(9,2)});

        executiveNameTextView = (TextView) findViewById(R.id.text_view_executive_name);
        cmsAmountTextView = (TextView) findViewById(R.id.text_view_cms_amount);
        serviceChargeTextView = (TextView) findViewById(R.id.text_view_service_charge);
        otwTextView = (TextView) findViewById(R.id.text_view_otw);
        totalTextView = (TextView) findViewById(R.id.text_view_total);
        paymentTextView = (TextView) findViewById(R.id.text_view_payment_type);
        noDataTextView = (TextView) findViewById(R.id.no_data_text_view);
        selectedItemsTotalTextView = (TextView) findViewById(R.id.selected_total_amount);
        cmsAmountTextView.setText("");
        serviceChargeTextView.setText("");
        otwTextView.setText("");
        totalTextView.setText("");
        selectedItemsTotalTextView.setText("Total: 00.0");
        selectedItemsTotalTextView.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new CustomAdapter(this, new ArrayList<PaymentTypeDetails>()));
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PaymentTypeDetails aCMSList = paymentTypeDetailsArrayList.get(position);
                aCMSList.setIsSelected(!aCMSList.isSelected());
                reloadListView();
            }
        });
        setListViewHeightBasedOnChildren(listView);
        refreshPaymentEntry(null);
        pixelHeight = convertDpToPixel(130, getApplicationContext());

    }
    private float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private void reloadListView() {
        if(paymentTypeDetailsArrayList != null) {
            if(paymentTypeDetailsArrayList.size() == 0)
            {
                noDataTextView.setVisibility(View.VISIBLE);
                selectedItemsTotalTextView.setVisibility(View.GONE);
            }
            else {
                noDataTextView.setVisibility(View.GONE);
                selectedItemsTotalTextView.setVisibility(View.VISIBLE);
            }
        }
        totalAmount = 0.0;
        for (PaymentTypeDetails paymentTypeDetails : paymentTypeDetailsArrayList) {
            if (paymentTypeDetails.isSelected()) {
                totalAmount = totalAmount + paymentTypeDetails.getPaymentAmount();
            }
        }
        selectedItemsTotalTextView.setText("Total: " +totalAmount);
        CustomAdapter customAdapter = (CustomAdapter) listView.getAdapter();
        customAdapter.setUpdateDataList(paymentTypeDetailsArrayList);
        customAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listView);
        listView.invalidateViews();
        listView.scrollBy(0, 0);

    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        CustomAdapter listAdapter = (CustomAdapter)listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            listItem.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<PaymentTypeDetails> paymentTypeArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(PaymentEntryActivity cmsActivity, ArrayList<PaymentTypeDetails> paymentTypeLists) {
            paymentTypeArrayList =paymentTypeLists;
            context=cmsActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<PaymentTypeDetails> paymentTypeLists)
        {
            paymentTypeArrayList =paymentTypeLists;
        }
        @Override
        public int getCount() {
            if(paymentTypeArrayList == null)
                return 0;
            return paymentTypeArrayList.size();
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
            TextView date, cmsAmount;
            CheckBox checkBox;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder;
            if(convertView == null) {
                holder = new Holder();

                convertView = inflater.inflate(R.layout.activity_payment_entry_list_item, null);
                holder.date =(TextView) convertView.findViewById(R.id.date);
                holder.cmsAmount = (TextView) convertView.findViewById(R.id.cmsAmount);
                holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkbox);
                convertView.setTag(holder);
            }
            else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (Holder) convertView.getTag();
            }
            holder.checkBox.setTag(position);

            PaymentTypeDetails aPaymentType = paymentTypeArrayList.get(position);
            holder.date.setText("Date: " + aPaymentType.getPaymentDate());
            holder.cmsAmount.setText("Amount: " + aPaymentType.getPaymentAmount());
            if(aPaymentType.getPaymentDate().equalsIgnoreCase("0"))
                holder.checkBox.setVisibility(View.GONE);
            else
                holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(aPaymentType.isSelected());

            return convertView;
        }

    }

    public void selectCMSList(View view)
    {
        PaymentTypeDetails aCMSList = paymentTypeDetailsArrayList.get((int) view.getTag());
        aCMSList.setIsSelected(!aCMSList.isSelected());
        reloadListView();
    }

    public void refreshPaymentEntry(View view)
    {
        progressDialog = ProgressDialog.show(PaymentEntryActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    private void reloadData()
    {
        ArrayList<Payment> paymentArrayList = Elixir.getPaymentList();
        executiveNames = new ArrayList<String>();
        for(int i=0; i< paymentArrayList.size();i++)
        {
            Payment aPayment = paymentArrayList.get(i);
            executiveNames.add(aPayment.getName());
        }
        executiveNameTextView.setText("Select Executive Name");
        paymentTextView.setText("Select Payment Type");
        cmsAmountTextView.setText("0.0");
        serviceChargeTextView.setText("0.0");
        otwTextView.setText("0.0");

    }

    public void showExecutiveNameDialog(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Executive Name");
        CharSequence[] cs = executiveNames.toArray(new CharSequence[executiveNames.size()]);

        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                executiveNameTextView.setText(executiveNames.get(which));
                executiveNameValue = which + 1;
                selectedPayment = Elixir.getPaymentList().get(which);
                cmsAmountTextView.setText(selectedPayment.getCms() + "");
                serviceChargeTextView.setText(selectedPayment.getService() + "");
                otwTextView.setText(selectedPayment.getOtw()+"");
                Double total = selectedPayment.getCms() + selectedPayment.getService() + selectedPayment.getOtw();
                totalTextView.setText(total + "");
                if(paymentTypeValue != -1)
                {
                    switch (paymentTypeValue)
                    {
                        case 0:
                        {
                            paymentTypeDetailsArrayList = selectedPayment.getServiceList();
                            break;
                        }
                        case 1:
                        {
                            paymentTypeDetailsArrayList = selectedPayment.getCmsList();
                            break;
                        }
                        case 2:
                        {
                            paymentTypeDetailsArrayList = selectedPayment.getOtwList();
                            break;
                        }
                        default:
                            break;
                    }
                    reloadListView();
                }

            }
        });
        builder.show();
    }

    public void showPaymentTypeDialog(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Payment Type");
        builder.setItems(paymentType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                paymentTextView.setText(paymentType[which]);
                paymentTypeValue = which;
                if(selectedPayment == null)
                    return;
                switch (which)
                {
                    case 0:
                    {
                        paymentTypeDetailsArrayList = selectedPayment.getServiceList();
                        break;
                    }
                    case 1:
                    {
                        paymentTypeDetailsArrayList = selectedPayment.getCmsList();
                        break;
                    }
                    case 2:
                    {
                        paymentTypeDetailsArrayList = selectedPayment.getOtwList();
                        break;
                    }
                    default:
                        break;
                }

                reloadListView();
            }
        });
        builder.show();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            if(apiHelper == null)
                apiHelper = new APIHelpers();
            if(isSendRequest)
                apiHelper.sendPaymentEntry(aPaymentEntry, aUser, getApplicationContext(), handler);
            else
                apiHelper.getPaymentEntryDetails(aUser, getApplicationContext(), handler);
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
                if(isSendRequest)
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(PaymentEntryActivity.this);
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
                else
                    reloadData();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(PaymentEntryActivity.this);
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

    public void savePaymentEntryDetails(View view)
    {
        String chequeAmount = chequeAmountEditText.getText().toString().trim();
        String cashAmount = cashAmountEditText.getText().toString().trim();

        String remarks = remarksEditText.getText().toString().trim();
        if(selectedPayment == null || paymentTypeDetailsArrayList == null)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please select Executive and payment Type.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();

            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }

        ArrayList<PaymentTypeDetails> selectedOTWPaymentTypeDetailsArrayList = new ArrayList<PaymentTypeDetails>();

        for (PaymentTypeDetails paymentTypeDetails : paymentTypeDetailsArrayList) {
            if (paymentTypeDetails.isSelected()) {
                selectedOTWPaymentTypeDetailsArrayList.add(paymentTypeDetails);
            }
        }
        if (selectedOTWPaymentTypeDetailsArrayList.size() == 0)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please select items from list.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();

            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }

        if(chequeAmount.equals("") && cashAmount.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please enter cheque/cash amount.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();

            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }

        if(chequeAmount.equals(""))
            chequeAmount = "0";
        if(cashAmount.equals(""))
            cashAmount = "0";

        aPaymentEntry = new PaymentEntry();
        switch (paymentTypeValue)
        {
            case 0:
            {
                aPaymentEntry.setPaymentType("service");
                break;
            }
            case 1:
            {
                aPaymentEntry.setPaymentType("cms");
                break;
            }
            case 2:
            {
                aPaymentEntry.setPaymentType("otw");
                break;
            }
            default:
                break;
        }
        aPaymentEntry.setExecutiveUserId(selectedPayment.getId());
        aPaymentEntry.setChequeAmount(Double.parseDouble(chequeAmount));
        aPaymentEntry.setCashAmount(Double.parseDouble(cashAmount));
        aPaymentEntry.setPaidAmount(aPaymentEntry.getCashAmount() + aPaymentEntry.getChequeAmount());
        aPaymentEntry.setTotalAmount(totalAmount);
        aPaymentEntry.setDifferenceAmount(aPaymentEntry.getTotalAmount() - aPaymentEntry.getPaidAmount());
        aPaymentEntry.setRemarks(remarks);
        aPaymentEntry.setPaymentTypeDetailsArrayList(selectedOTWPaymentTypeDetailsArrayList);

        double diff = totalAmount - aPaymentEntry.getPaidAmount();
        if(diff != 0 )
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Paid Amount is " + aPaymentEntry.getPaidAmount() + ". Differnce amount between Total amount and Paid amount is "+Math.abs(diff) + ". Do you want to save?")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    savePaymenEntry();
                }
            });
            alert.setButton(-2, "NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alert.setTitle("Error");
            alert.show();
            return;

        }
        else {
            savePaymenEntry();
        }
    }

    private void savePaymenEntry()
    {
        isSendRequest = true;
        progressDialog = ProgressDialog.show(PaymentEntryActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void cancelEntryView(View view)
    {
        finish();
    }
}
