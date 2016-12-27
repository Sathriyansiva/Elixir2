package com.invicibledevs.elixir;

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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.PolicyDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.DecimalDigitsInputFilter;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.Policy;
import com.invicibledevs.elixir.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddPolicyActivity extends AppCompatActivity implements Runnable{

    private Button saveButton, sendPhotosButton;
    private TextView companyNameTextView, vehicleTypeTextView, ncbTextView, previousInsuranceTextView, dateOfRegTextView, riskStartDateTextView, documentImageHeaderTextView, yearOfManufactureTextView;
    private EditText customerNameEditText, mobileNoEditText, addressEditText, vehicleNoEditText, engineNoEditText, chasisNoEditText, idvEditText, ccEditText, odDiscountEditText,premiumAmountEditText, makeTextView, modelTextView, serviceChargeEditText, financierNameEditText, previousInsuranceNoEditText, emailEditText;
    private int companyValue=0, vehicleTypeValue=0, ncbValue=0, previousInsuranceValue=0, makeValue=0, modelValue=0;

    private JSONArray makes, models, previousInsurance, companyName, vehicleType, ncb;
    private String[] previousYears;
    private User aUser;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;
    private Policy thePolicy;
    private RadioGroup previousInsuranceRadioGroup, finaceRadioGroup, payementTypeRadioGroup, policyTypeRadioGroup, prepareByRadioGroup, imtRadioGroup, nilDIPRadioGroup, claimsRadioGroup, nameTransferRadioGroup;
    private PolicyDataSource thePolicyDataSource;
    private RadioButton selfPrepareByRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_policy);

        previousYears = getPreviousThirtyYears();
        saveButton = (Button) findViewById(R.id.btn_save);
        sendPhotosButton = (Button) findViewById(R.id.btn_send_photos);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));

        companyNameTextView =(TextView) findViewById(R.id.text_company_name);
        vehicleTypeTextView =(TextView) findViewById(R.id.text_vehicle_type);
        ncbTextView = (TextView) findViewById(R.id.text_view_ncb);
        ncbTextView.setEnabled(false);
        previousInsuranceTextView = (TextView) findViewById(R.id.text_view_previos_insurance);
        previousInsuranceTextView.setEnabled(false);
        dateOfRegTextView = (TextView) findViewById(R.id.text_view_date_of_reg);
        riskStartDateTextView = (TextView) findViewById(R.id.text_view_risk_start_date);

        customerNameEditText = (EditText) findViewById(R.id.editText_customer_name);
        mobileNoEditText = (EditText) findViewById(R.id.editText_mobile_no);
        addressEditText = (EditText) findViewById(R.id.editText_address);
        vehicleNoEditText = (EditText) findViewById(R.id.editText_vehicle_no);
        engineNoEditText = (EditText) findViewById(R.id.editText_engine_no);
        chasisNoEditText = (EditText) findViewById(R.id.editText_chasis_no);
        makeTextView = (EditText) findViewById(R.id.editText_make);
        modelTextView = (EditText) findViewById(R.id.editText_model);
        yearOfManufactureTextView = (TextView) findViewById(R.id.editText_year_of_manufacture);
        idvEditText = (EditText) findViewById(R.id.editText_idv);
        ccEditText = (EditText) findViewById(R.id.editText_cc);
        odDiscountEditText = (EditText) findViewById(R.id.editText_od_discount);
        premiumAmountEditText = (EditText) findViewById(R.id.editText_premium_amount);
        serviceChargeEditText = (EditText) findViewById(R.id.editText_service_charge);
        financierNameEditText = (EditText) findViewById(R.id.editText_financier_name);
        financierNameEditText.setEnabled(false);

        previousInsuranceNoEditText = (EditText) findViewById(R.id.editText_previous_insurance_no);
        emailEditText = (EditText) findViewById(R.id.editText_e_mail);

        idvEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        premiumAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8,2)});
        serviceChargeEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6,2)});

        previousInsuranceRadioGroup = (RadioGroup) findViewById(R.id.checkPreviousInsuranceRadioGroup);
        finaceRadioGroup = (RadioGroup) findViewById(R.id.financeRadioGroup);
        payementTypeRadioGroup = (RadioGroup) findViewById(R.id.paymentTypeRadioGroup);
        policyTypeRadioGroup = (RadioGroup) findViewById(R.id.policyTypeRadioGroup);
        prepareByRadioGroup = (RadioGroup) findViewById(R.id.prepareByRadioGroup);
        imtRadioGroup = (RadioGroup) findViewById(R.id.imtRadioGroup);
        nilDIPRadioGroup = (RadioGroup) findViewById(R.id.nilDipRadioGroup);
        claimsRadioGroup = (RadioGroup) findViewById(R.id.claimsRadioGroup);
        nameTransferRadioGroup = (RadioGroup) findViewById(R.id.nameTransferRadioGroup);

        selfPrepareByRadioButton = (RadioButton) findViewById(R.id.self);
        if(!elixirPreferences.getString("permissionStatus", "").equalsIgnoreCase("yes"))
            selfPrepareByRadioButton.setVisibility(View.GONE);

        previousInsuranceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                previosInsuranceClickAction(null);
            }
        });

        finaceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                finaceRadioGropClickAction(null);
            }
        });

        claimsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                claimsClickAction(null);
            }
        });

        nameTransferRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                nameTransferClickAction(null);
            }
        });

        payementTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                paymentTypeRadioGropClickAction(null);
            }
        });


        if(thePolicyDataSource == null)
            thePolicyDataSource = new PolicyDataSource(getApplicationContext());

        previousInsuranceTextView.setEnabled(false);
        previousInsuranceNoEditText.setEnabled(false);

        RadioButton rb11 = (RadioButton) findViewById(R.id.claimsNo);
        rb11.setEnabled(false);
        RadioButton rb12 = (RadioButton) findViewById(R.id.claimsYes);
        rb12.setEnabled(false);

        RadioButton rb21 = (RadioButton) findViewById(R.id.nameTransferNo);
        rb21.setEnabled(false);
        RadioButton rb22 = (RadioButton) findViewById(R.id.nameTransferYes);
        rb22.setEnabled(false);

        RadioButton rb31 = (RadioButton) findViewById(R.id.imtNo);
        rb31.setEnabled(false);
        RadioButton rb32 = (RadioButton) findViewById(R.id.imtYes);
        rb32.setEnabled(false);

        makes = Elixir.getMakeJsonArray();
        previousInsurance = Elixir.getPreviousInsuranceArrayList();
        companyName = Elixir.getCompanyArrayList();
        vehicleType = Elixir.getVehicleArrayList();
        ncb = Elixir.getNcbArrayList();

//        sendPhotosButton.setEnabled(true);
    }

    public void showNCBDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a NCB");
        builder.setAdapter(new CustomAdapter(this, ncb), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObj = ncb.getJSONObject(which);
                    ncbTextView.setText(jsonObj.getString("ncb"));
                    ncbValue = jsonObj.getInt("id");
                } catch (JSONException exception) {

                }
            }
        });
        builder.show();


    }

    public void showVehicleTypePickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Vehicle Type");
        builder.setAdapter(new CustomAdapter(this, vehicleType), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObj = vehicleType.getJSONObject(which);
                    vehicleTypeTextView.setText(jsonObj.getString("vehicletype"));
                    vehicleTypeValue = jsonObj.getInt("id");

                    if(vehicleTypeTextView.getText().toString().equalsIgnoreCase("PCCV above 6 Seates") || vehicleTypeTextView.getText().toString().equalsIgnoreCase("GCCV"))
                    {
                        RadioButton rb31 = (RadioButton) findViewById(R.id.imtNo);
                        rb31.setEnabled(true);
                        RadioButton rb32 = (RadioButton) findViewById(R.id.imtYes);
                        rb32.setEnabled(true);
                    }
                    else
                    {
                        RadioButton rb31 = (RadioButton) findViewById(R.id.imtNo);
                        rb31.setEnabled(false);
                        rb31.setChecked(true);
                        RadioButton rb32 = (RadioButton) findViewById(R.id.imtYes);
                        rb32.setEnabled(false);
                    }
                } catch (JSONException exception) {

                }
            }
        });
        builder.show();
    }

    public void showCompanyNamePickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Company Name");
        builder.setAdapter(new CustomAdapter(this, companyName), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObj = companyName.getJSONObject(which);
                    companyNameTextView.setText(jsonObj.getString("company"));
                    companyValue = jsonObj.getInt("id");
                } catch (JSONException exception) {

                }
            }
        });
        builder.show();
    }

    public void showPreviousInsuranceDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Previous Insurance");
        builder.setAdapter(new CustomAdapter(this, previousInsurance), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObj = previousInsurance.getJSONObject(which);
                    previousInsuranceTextView.setText(jsonObj.getString("previnsur"));
                    previousInsuranceValue = jsonObj.getInt("id");
                } catch (JSONException exception) {

                }
            }
        });
        builder.show();
    }

    public void showMakePickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Make");
        builder.setAdapter(new CustomAdapter(this, makes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObj = makes.getJSONObject(which);
                    makeTextView.setText(jsonObj.getString("make"));
                    makeValue = jsonObj.getInt("makeid");
                    models = jsonObj.getJSONArray("model");
                    modelTextView.setText("");
                    modelValue = -1;
                } catch (JSONException exception) {

                }
            }
        });
        builder.show();
    }

    public void showModelPickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Model");
        builder.setAdapter(new CustomAdapter(this, models), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    JSONObject jsonObj = models.getJSONObject(which);
                    modelTextView.setText(jsonObj.getString("model"));
                    modelValue = jsonObj.getInt("modelid");

                } catch (JSONException exception) {

                }
            }
        });
        builder.show();
    }

    public void showYearOfManufacturePickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Manufacture Year");
        builder.setItems(previousYears, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                yearOfManufactureTextView.setText(previousYears[which]);
            }
        });
        builder.show();
    }

    public void previosInsuranceClickAction(View v)
    {
        if(previousInsuranceRadioGroup.getCheckedRadioButtonId() == R.id.yes)
        {
            ncbTextView.setEnabled(true);
            previousInsuranceTextView.setEnabled(true);
            previousInsuranceNoEditText.setEnabled(true);

            RadioButton rb11 = (RadioButton) findViewById(R.id.claimsNo);
            rb11.setEnabled(true);
            RadioButton rb12 = (RadioButton) findViewById(R.id.claimsYes);
            rb12.setEnabled(true);

            RadioButton rb21 = (RadioButton) findViewById(R.id.nameTransferNo);
            rb21.setEnabled(true);
            RadioButton rb22 = (RadioButton) findViewById(R.id.nameTransferYes);
            rb22.setEnabled(true);

        } else
        {
            ncbTextView.setEnabled(false);
            ncbTextView.setText("");
            ncbValue = -1;
            previousInsuranceTextView.setEnabled(false);
            previousInsuranceTextView.setText("");
            previousInsuranceNoEditText.setEnabled(false);
            previousInsuranceNoEditText.setText("");
            previousInsuranceValue = -1;
            RadioButton rb11 = (RadioButton) findViewById(R.id.claimsNo);
            rb11.setChecked(true);
            rb11.setEnabled(false);
            RadioButton rb12 = (RadioButton) findViewById(R.id.claimsYes);
            rb12.setEnabled(false);

            RadioButton rb21 = (RadioButton) findViewById(R.id.nameTransferNo);
            rb21.setChecked(true);
            rb21.setEnabled(false);
            RadioButton rb22 = (RadioButton) findViewById(R.id.nameTransferYes);
            rb22.setEnabled(false);
        }
    }

    public void claimsClickAction(View v)
    {
        if(claimsRadioGroup.getCheckedRadioButtonId() == R.id.claimsNo)
        {
            ncbTextView.setEnabled(true);
            previousInsuranceTextView.setEnabled(true);
            previousInsuranceNoEditText.setEnabled(true);

            RadioButton rb21 = (RadioButton) findViewById(R.id.nameTransferNo);
            rb21.setEnabled(true);
            rb21.setSelected(true);
            RadioButton rb22 = (RadioButton) findViewById(R.id.nameTransferYes);
            rb22.setEnabled(true);

        } else
        {
            ncbTextView.setEnabled(false);
            ncbTextView.setText("");
            ncbValue = -1;
            previousInsuranceTextView.setEnabled(false);
            previousInsuranceTextView.setText("");
            previousInsuranceValue = -1;
            previousInsuranceNoEditText.setEnabled(false);
            previousInsuranceNoEditText.setText("");


            RadioButton rb21 = (RadioButton) findViewById(R.id.nameTransferNo);
            rb21.setChecked(true);
            rb21.setEnabled(false);
            RadioButton rb22 = (RadioButton) findViewById(R.id.nameTransferYes);
            rb22.setEnabled(false);

        }
    }

    public void nameTransferClickAction(View v)
    {
        if(nameTransferRadioGroup.getCheckedRadioButtonId() == R.id.nameTransferNo)
        {
            ncbTextView.setEnabled(true);
            previousInsuranceTextView.setEnabled(true);
            previousInsuranceNoEditText.setEnabled(true);

        } else
        {
            ncbTextView.setEnabled(false);
            ncbTextView.setText("");
            ncbValue = -1;
            previousInsuranceTextView.setEnabled(false);
            previousInsuranceTextView.setText("");
            previousInsuranceValue = -1;
            previousInsuranceNoEditText.setEnabled(false);
            previousInsuranceNoEditText.setText("");
        }
    }


    public void finaceRadioGropClickAction(View v)
    {
        if(finaceRadioGroup.getCheckedRadioButtonId() == R.id.finance_yes)
        {
            financierNameEditText.setEnabled(true);
        }
        else
        {
            financierNameEditText.setEnabled(false);
            financierNameEditText.setText("");
        }
    }

    public void paymentTypeRadioGropClickAction(View v)
    {
//        if(payementTypeRadioGroup.getCheckedRadioButtonId() == R.id.cheque)
//        {
//            String redString = getResources().getString(R.string.warningms);
//
//            documentImageHeaderTextView.setText("Add Documents Image"+ Html.fromHtml(redString));
//
//        }
//        else
//        {
//            documentImageHeaderTextView.setText("Add Documents Image");
//        }
    }

    public void showDateOfRegistrationPickerDialog(View v)
    {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aAddPolicyActivity = this;
        newFragment.isDateOfReg = true;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showRiskDatePickerDialog(View v)
    {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aAddPolicyActivity = this;
        newFragment.isDateOfReg = false;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public AddPolicyActivity aAddPolicyActivity;
        public boolean isDateOfReg;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = 0;
            int month = 0;
            int day = 0;
            Date date = aAddPolicyActivity.getDateFromString("dd/MM/yyyy", aAddPolicyActivity.getRiskDateString());
            if(isDateOfReg)
                date = aAddPolicyActivity.getDateFromString("dd/MM/yyyy", aAddPolicyActivity.getDateOfRegString());
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
                dp.setMinDate(timeInMills);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if(isDateOfReg)
                aAddPolicyActivity.showDateOfReg(year, month + 1, day);
            else
                aAddPolicyActivity.showRiskStartDate(year, month + 1, day);
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

        riskStartDateTextView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String getRiskDateString()
    {
        return riskStartDateTextView.getText().toString();
    }

    private void showDateOfReg(int year, int month, int day) {

        dateOfRegTextView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String getDateOfRegString()
    {
        return dateOfRegTextView.getText().toString();
    }

    public void savePolicyDetails(View view)
    {
        String customerName = customerNameEditText.getText().toString().trim();
        String mobileNo = mobileNoEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String companyName = companyNameTextView.getText().toString().trim();
        String vehicleType = vehicleTypeTextView.getText().toString().trim();
        String vehicleNo = vehicleNoEditText.getText().toString().trim();
        String engineNo = engineNoEditText.getText().toString().trim();
        String chasisNo = chasisNoEditText.getText().toString().trim();
        String dateOfReg = dateOfRegTextView.getText().toString().trim();
        String make = makeTextView.getText().toString().trim();
        String model = modelTextView.getText().toString().trim();
        String idv = idvEditText.getText().toString().trim();
        String cc = ccEditText.getText().toString().trim();
        String ncb = ncbTextView.getText().toString().trim();
        String previousInsurance = previousInsuranceTextView.getText().toString().trim();
        String odDiscount = odDiscountEditText.getText().toString().trim();
        String premiumAmount = premiumAmountEditText.getText().toString().trim();
        String serviceCharge = serviceChargeEditText.getText().toString().trim();
        String riskStartDate = riskStartDateTextView.getText().toString().trim();
        String finacierName = financierNameEditText.getText().toString().trim();
        String manufactureYear = yearOfManufactureTextView.getText().toString().trim();
        String previousInsuranceNo = previousInsuranceNoEditText.getText().toString().trim();
        String emailId = emailEditText.getText().toString().trim();

        RadioButton radioFinanceButton = (RadioButton) findViewById(finaceRadioGroup.getCheckedRadioButtonId());
        RadioButton radioPreviousInsuranceButton = (RadioButton) findViewById(previousInsuranceRadioGroup.getCheckedRadioButtonId());

        RadioButton radioImtButton = (RadioButton) findViewById(imtRadioGroup.getCheckedRadioButtonId());
        RadioButton radioNilDIPButton = (RadioButton) findViewById(nilDIPRadioGroup.getCheckedRadioButtonId());
        RadioButton radioClaimsButton = (RadioButton) findViewById(claimsRadioGroup.getCheckedRadioButtonId());
        RadioButton radioNameTransferButton = (RadioButton) findViewById(nameTransferRadioGroup.getCheckedRadioButtonId());

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
        if(customerName.equals("") || mobileNo.equals("") || address.equals("") || manufactureYear.equals("") || vehicleNo.equals("") || premiumAmount.equals("") || companyName.equals("") || vehicleType.equals("") || engineNo.equals("") || chasisNo.equals("") || dateOfReg.equals("")|| make.equals("") || model.equals("") || idv.equals("")
                || cc.equals("") || riskStartDate.equals("") || odDiscount.equals("") || premiumAmount.equals("") || serviceCharge.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Enter All madatory fields except email.")
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
        else if(radioFinanceButton.getId() == R.id.finance_yes && finacierName.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please enter financier name.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        else if(radioPreviousInsuranceButton.getId() == R.id.yes && radioClaimsButton.getId() == R.id.claimsNo && radioNameTransferButton.getId() == R.id.nameTransferNo && (previousInsurance.equals("") || previousInsuranceNo.equals("") || ncb.equals("")))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please select NCB & Previous Insurance.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        else if(emailId.length() > 0 && !isValidEmail(emailEditText.getText()))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Please enter valid email.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        Policy aPolicy = new Policy();
        aPolicy.setUserId(aUser.getUserId());
        aPolicy.setCustomerName(customerName);
        aPolicy.setMobileNo(mobileNo);
        aPolicy.setAddress(address);
        aPolicy.setCompanyName(companyValue + "");
        aPolicy.setVehicleType(vehicleTypeValue + "");
        aPolicy.setVehicleNo(vehicleNo);
        aPolicy.setEngineNo(engineNo);
        aPolicy.setChasisNo(chasisNo);
        aPolicy.setDateOfReg(dateOfReg);
        aPolicy.setMake(make);
        aPolicy.setModel(model);
        if(!idv.equals(""))
            aPolicy.setIdv(Double.parseDouble(idv));
        else
            aPolicy.setIdv(0);
        aPolicy.setCc(cc);

        aPolicy.setIsHavingPreviousInsurance(false);
        if(previousInsuranceRadioGroup.getCheckedRadioButtonId() == R.id.yes)
            aPolicy.setIsHavingPreviousInsurance(true);
        aPolicy.setNcb(ncbValue + "");
        aPolicy.setPreviousInsurance(previousInsuranceValue + "");
        if(!odDiscount.equals(""))
            aPolicy.setOdDiscount(Integer.parseInt(odDiscount));
        else
            aPolicy.setOdDiscount(0);

        if(!premiumAmount.equals(""))
            aPolicy.setPreminumAmount(Double.parseDouble(premiumAmount));
        else
            aPolicy.setPreminumAmount(0);
        if(!serviceCharge.equals(""))
            aPolicy.setServiceCharege(Double.parseDouble(serviceCharge));
        else
            aPolicy.setServiceCharege(0);
        aPolicy.setRiskStartDate(riskStartDate);
        aPolicy.setIsHavingFinance(false);
        if(finaceRadioGroup.getCheckedRadioButtonId() == R.id.finance_yes)
            aPolicy.setIsHavingFinance(true);
        aPolicy.setFinancierName(finacierName);
        RadioButton radioPaymentTypeButton = (RadioButton) findViewById(payementTypeRadioGroup.getCheckedRadioButtonId());
        RadioButton radioPolicyTypeButton = (RadioButton) findViewById(policyTypeRadioGroup.getCheckedRadioButtonId());
        RadioButton radioPolicyPrepareByButton = (RadioButton) findViewById(prepareByRadioGroup.getCheckedRadioButtonId());

        aPolicy.setPaymentType(radioPaymentTypeButton.getText().toString());
        aPolicy.setPolicyType(radioPolicyTypeButton.getText().toString());
        aPolicy.setPolicyPreparedBy(radioPolicyPrepareByButton.getText().toString());

        aPolicy.setYearOfManufacture(manufactureYear);
        aPolicy.setPreviousInsuranceNo(previousInsuranceNo);
        aPolicy.setEmailId(emailId);

        if(imtRadioGroup.getCheckedRadioButtonId() == R.id.imtYes)
            aPolicy.setImtStatus(true);
        if(nilDIPRadioGroup.getCheckedRadioButtonId() == R.id.nilDipYes)
            aPolicy.setNilDIPStatus(true);
        if(claimsRadioGroup.getCheckedRadioButtonId() == R.id.claimsYes)
            aPolicy.setClaimsStatus(true);
        if(nameTransferRadioGroup.getCheckedRadioButtonId() == R.id.nameTransferYes)
            aPolicy.setNameTransferStatus(true);
        thePolicy = aPolicy;

        progressDialog = ProgressDialog.show(AddPolicyActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void showPolicyPhotosView(View view)
    {
        Intent mainIntent = new Intent(this, PhotosPolicyActivity.class);

        mainIntent.putExtra("policyId",thePolicy.getPolicyId());
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

    public void cancelAddPolicyView(View view)
    {
        if(thePolicy != null && thePolicy.getPolicyId()!= null) {
            confirmAndCloseView();
        }
        else
            finish();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            if(apiHelper == null)
                apiHelper = new APIHelpers();
            apiHelper.sendPolicy(thePolicy, aUser, getApplicationContext(), handler);
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
        public String policyId;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            policyId = bundle.getString("policyId");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(AddPolicyActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                            thePolicy.setPolicyId(policyId);
                            if(!thePolicy.getPolicyPreparedBy().equalsIgnoreCase("self"))
                                thePolicyDataSource.createPolicy(thePolicy);
                            saveButton.setEnabled(false);
                            sendPhotosButton.setEnabled(true);
                            showPolicyPhotosView(null);
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
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(AddPolicyActivity.this);
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(thePolicy != null && thePolicy.getPolicyId()!= null) {
                confirmAndCloseView();
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void confirmAndCloseView()
    {

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("Please make sure, you have uploaded all imagse. If you cancel this screen, you can't upload images again. Do you want to cancel?")
                .setCancelable(true);
        AlertDialog alert = alt_bld.create();
        alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setButton(-2, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setTitle("Confirm");
        alert.show();

    }

    private String[] getPreviousThirtyYears() {
        String[] years = new String[30];
        for (int i=0; i<30; i++) {
            Calendar prevYear = Calendar.getInstance();
            prevYear.add(Calendar.YEAR, -i);
            years[i] = prevYear.get(Calendar.YEAR)+"";
        }
        return years;
    }

    public class CustomAdapter extends BaseAdapter {
        JSONArray jsonCustomArray;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(AddPolicyActivity addPolicyActivity, JSONArray jsonArray) {
            jsonCustomArray = jsonArray;
            context=addPolicyActivity;

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

                if(aJSONObj.has("make"))
                    holder.titleTextView.setText(aJSONObj.getString("make"));
                else if(aJSONObj.has("model"))
                    holder.titleTextView.setText(aJSONObj.getString("model"));
                else if(aJSONObj.has("company"))
                    holder.titleTextView.setText(aJSONObj.getString("company"));
                else if(aJSONObj.has("vehicletype"))
                    holder.titleTextView.setText(aJSONObj.getString("vehicletype"));
                else if(aJSONObj.has("previnsur"))
                    holder.titleTextView.setText(aJSONObj.getString("previnsur"));
                else if(aJSONObj.has("ncb"))
                    holder.titleTextView.setText(aJSONObj.getString("ncb"));

            }
            catch (JSONException exception)
            {

            }
            return convertView;
        }

    }

    private boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
