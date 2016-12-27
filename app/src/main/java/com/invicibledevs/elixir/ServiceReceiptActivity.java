package com.invicibledevs.elixir;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.invicibledevs.elixir.dao.ServiceReceiptDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.DecimalDigitsInputFilter;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.ServiceReceipt;
import com.invicibledevs.elixir.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ServiceReceiptActivity extends AppCompatActivity implements Runnable{
    static final int REQUEST_SIGNATURE = 1, REQUEST_CAMERA = 2, SELECT_FILE = 3;
    private TextView companyNameTextView, policyPeriodTextView, vehicleTypeTextView;
    private EditText nameEditText, mobileNoEditText, vehicleNoEditText, serviceChargeEditText, emailEditText,policyNoEditText, policyAmountEditText, odEditText;
    private ImageButton addImageOptionButton, signatureImageButton;
    private Button addImageButton, addSignatureButton;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;
    private ServiceReceipt theServiceReceipt;
    private User aUser;
    private ServiceReceiptDataSource theServiceReceiptDataSource;
    private byte[] image1, image2, image3, signatureImage;
    private int policyPeriodValue=0, vehicleTypeValue=0, companyValue=0;
    private RadioGroup receiptRequestRadioGroup;
    private boolean saveImagesLocally;
    private String currentDate;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 10, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;
    private JSONArray companyName, vehicleType, policyPeriod;
    private String pictureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_receipt);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));

        companyNameTextView =(TextView) findViewById(R.id.text_company_name);
        policyPeriodTextView = (TextView)findViewById(R.id.text_view_policy_period);
        vehicleTypeTextView = (TextView) findViewById(R.id.text_vehicle_type);
        addImageButton = (Button) findViewById(R.id.btn_add_image);
        nameEditText = (EditText) findViewById(R.id.editText_name);
        mobileNoEditText = (EditText) findViewById(R.id.editText_mobile_no);
        vehicleNoEditText = (EditText) findViewById(R.id.editText_vehicle_no);
        serviceChargeEditText = (EditText) findViewById(R.id.editText_service_charge);
        emailEditText = (EditText) findViewById(R.id.editText_e_mail);
        policyNoEditText = (EditText) findViewById(R.id.editText_policy_no);
        policyAmountEditText = (EditText) findViewById(R.id.editText_policy_amount);
        odEditText = (EditText) findViewById(R.id.editText_od);
        receiptRequestRadioGroup = (RadioGroup) findViewById(R.id.serviceReceiptRequestRadioGroup);

        addSignatureButton = (Button) findViewById(R.id.btn_add_signature_image);
        signatureImageButton = (ImageButton) findViewById(R.id.signatureImageView);

        serviceChargeEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6,2)});
        policyAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(9,2)});
        odEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(9,2)});

        saveImagesLocally = false;

        if(theServiceReceiptDataSource == null)
            theServiceReceiptDataSource = new ServiceReceiptDataSource(getApplicationContext());

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = df.format(c.getTime());

        companyName = Elixir.getCompanyArrayList();
        vehicleType = Elixir.getVehicleArrayList();
        policyPeriod = Elixir.getPeriodJsonArray();
    }

    public void showPolicyPickerDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Policy Period");

        builder.setAdapter(new CustomAdapter(this, policyPeriod), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject jsonObj = policyPeriod.getJSONObject(which);
                    policyPeriodTextView.setText(jsonObj.getString("policyperiod"));
                    policyPeriodValue = jsonObj.getInt("id");
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

    public void showAddImageOptionView(View view)
    {
//        if(view.getId() == R.id.btn_add_image) {
//            addImageOptionButton = (ImageButton) findViewById(R.id.btn_image1);
//            if (addImageOptionButton.getVisibility() == View.VISIBLE) {
//                addImageOptionButton = (ImageButton) findViewById(R.id.btn_image2);
//                if (addImageOptionButton.getVisibility() == View.VISIBLE)
//                    addImageOptionButton = (ImageButton) findViewById(R.id.btn_image3);
//            }
//        }
//        else
            addImageOptionButton = (ImageButton)view;

        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ServiceReceiptActivity.this);
        builder.setTitle("Add Image!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {

                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(ServiceReceiptActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ServiceReceiptActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);

                    }
                    else {

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ElixirPDF");
                        if (!storageDir.exists()) {
                            File creDirectory = new File("/sdcard/ElixirPDF/");
                            creDirectory.mkdirs();
                            storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ElixirPDF/ServiceReceipt");
                            if (!storageDir.exists()) {
                                creDirectory = new File("/sdcard/ElixirPDF/ServiceReceipt/");
                                creDirectory.mkdirs();
                            }
                        }
                        pictureImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ElixirPDF/SR-" + System.currentTimeMillis() + ".jpg";
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pictureImagePath)));
                        intent.putExtra( android.provider.MediaStore.EXTRA_SIZE_LIMIT, "720000");
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    if (ContextCompat.checkSelfPermission(ServiceReceiptActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ServiceReceiptActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    }
                    else {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(intent, "Select File"),
                                SELECT_FILE);
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == REQUEST_SIGNATURE)
            {
                signatureImage = data.getByteArrayExtra("signatureImage");
                addSignatureButton.setVisibility(View.GONE);
                signatureImageButton.setVisibility(View.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeByteArray(signatureImage, 0, signatureImage.length);
                signatureImageButton.setImageBitmap(bitmap);
                    //Do whatever you want with yourData

            }
            else if (requestCode == REQUEST_CAMERA) {
//                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                try {
//                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//
//                }
//                catch (NullPointerException e)
//                {
//                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(ServiceReceiptActivity.this);
//                    alt_bld.setMessage("Null pointer exception.")
//                            .setCancelable(true);
//                    AlertDialog alert = alt_bld.create();
//                    alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                        } });
//                    alert.setTitle("Error");
//                    alert.show();
//                    return;
//                }
                Uri uri = null;

                try {
                    uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), pictureImagePath, null, null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(getApplicationContext(),"success..",Toast.LENGTH_LONG).show();
                Bitmap thumbnail = null;
                try {
                    thumbnail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    thumbnail = Elixir.imageOreintationValidator(thumbnail, pictureImagePath);
                    int[] Req_Dimension;
                    Req_Dimension=resize(thumbnail);
                    thumbnail = Bitmap.createScaledBitmap(thumbnail, Req_Dimension[0], Req_Dimension[1], true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
                if(thumbnail == null)
                    return;

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                if( this.addImageOptionButton.getId() == R.id.btn_image1)
                    image1 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image2)
                    image2 = bytes.toByteArray();
                else
                    image3 = bytes.toByteArray();
                File destination = new File(Environment.getExternalStorageDirectory()+ "/ElixirPDF/",System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.addImageOptionButton.setImageBitmap(thumbnail);
                this.addImageOptionButton.setVisibility(View.VISIBLE);
                this.addImageOptionButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                ((ViewGroup.MarginLayoutParams) addImageOptionButton.getLayoutParams()).rightMargin = 10;

//                if(addImageOptionButton.getId() == R.id.btn_image3)
//                    this.addImageButton.setVisibility(View.GONE);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri.toString().contains("file:"))
                    selectedImageUri = getImageContentUri(getApplicationContext(), new File(selectedImageUri.getPath()));
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                if(selectedImagePath.contains("http://") || selectedImagePath.contains("https://"))
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(ServiceReceiptActivity.this);
                    alt_bld.setMessage("Don't select Web images. Invalid image.")
                            .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        } });
                    alert.setTitle("Error");
                    alert.show();
                    return;
                }


                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                if(this.addImageOptionButton.getId() == R.id.btn_image1)
                    image1 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image2)
                    image2 = stream.toByteArray();
                else
                    image3 = stream.toByteArray();
                this.addImageOptionButton.setImageBitmap(bm);
                this.addImageOptionButton.setVisibility(View.VISIBLE);
                this.addImageOptionButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                ((ViewGroup.MarginLayoutParams) addImageOptionButton.getLayoutParams()).rightMargin = 10;

//                if(addImageOptionButton.getId() == R.id.btn_image3)
//                    this.addImageButton.setVisibility(View.GONE);
            }
        }
        File file = new File(pictureImagePath);
        file.delete();
    }

    public  int[] resize (Bitmap bitmap)
    {
        int[] Dimension = new int[2];
        int original_height = bitmap.getHeight();
        int original_width = bitmap.getWidth();

        int bound_width = 1200;
        int bound_height = 1600;

        int new_width=0;
        int new_height=0;

        if(original_width>bound_width && original_height>bound_height) {
            new_width=bound_width;
            new_height=bound_height;
        }
        else  if (original_width >= bound_width  && original_height <= bound_height)
        {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;

            // then check if we need to scale even with the new height
            if (new_height >= bound_height) {
                //scale height to fit instead
                new_height = bound_height;
                //scale width to maintain aspect ratio
                new_width = (new_height * original_width) / original_height;
            }

        }
        else if(original_width <= bound_width  && original_height>=bound_height)
        {
            new_height=bound_height;
            new_width=(new_height * original_width) / original_height;
            if (new_width >= bound_width) {
                //scale height to fit instead
                new_width = bound_width;
                //scale width to maintain aspect ratio
                new_height = (new_width * original_height) / original_width;
            }
        }
        else if (original_width < bound_width  && original_height<bound_height)
        {
            new_width = original_width;
            new_height=original_height;

        }
        Dimension[0]=new_width;
        Dimension[1]=new_height;
        return Dimension;

    }

    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public void showSyncView(View view)
    {
        Intent mainIntent = new Intent(this, SyncActivity.class);
        startActivity(mainIntent);
    }

    public void saveServiceReceiptDetails(View view)
    {
        String name = nameEditText.getText().toString().trim();
        String mobileNo = mobileNoEditText.getText().toString().trim();
        String vehicleNo = vehicleNoEditText.getText().toString().trim();
        String serviceCharge = serviceChargeEditText.getText().toString().trim();
        String vehicleType = vehicleTypeTextView.getText().toString().trim();
        String policyPeriod = policyPeriodTextView.getText().toString().trim();
        String companyName = companyNameTextView.getText().toString().trim();
        String od = odEditText.getText().toString().trim();

        String policyNo = policyNoEditText.getText().toString().trim();
        String policyAmt = policyAmountEditText.getText().toString().trim();

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
        if(name.equals("") || mobileNo.equals("") || vehicleNo.equals("") || serviceCharge.equals("") || vehicleType.equals("") || policyPeriod.equals("")
        || policyNo.equals("") || policyAmt.equals("") || od.equals("") || companyName.equals("") || image1 == null || image2 == null || image3 == null || signatureImage == null)
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("All fields are required except email.")
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
        ArrayList<ServiceReceipt> theServiceReceipts = theServiceReceiptDataSource.getAllServiceReceipts();
        if(theServiceReceipts.size()>20)
        {
            saveImagesLocally = false;
            saveServiceReceipt();
        }
        else {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Do you want to save images locally for further processing?")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveImagesLocally = true;
                    saveServiceReceipt();
                }
            });
            alert.setButton(-2, "NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    saveImagesLocally = false;
                    saveServiceReceipt();
                }
            });
            alert.setTitle("Confirm");
            alert.show();
        }

    }

    private void saveServiceReceipt()
    {
        ServiceReceipt aServiceReceipt = new ServiceReceipt();
        aServiceReceipt.setName(nameEditText.getText().toString());
        aServiceReceipt.setMobileNo(mobileNoEditText.getText().toString());
        aServiceReceipt.setVehicleNo(vehicleNoEditText.getText().toString());
        aServiceReceipt.setServiceCharge(Double.parseDouble(serviceChargeEditText.getText().toString()));
        aServiceReceipt.setVehicleType(vehicleTypeValue + "");
        aServiceReceipt.setPolicyPeriod(policyPeriodValue + "");
        aServiceReceipt.setCompanyName(companyValue + "");
        aServiceReceipt.setEmail(emailEditText.getText().toString());
        aServiceReceipt.setPolicyNo(policyNoEditText.getText().toString());
        aServiceReceipt.setPolicyAmount(Double.parseDouble(policyAmountEditText.getText().toString()));
        aServiceReceipt.setOd(Double.parseDouble(odEditText.getText().toString()));
        aServiceReceipt.setImage1(image1);
        aServiceReceipt.setImage2(image2);
        aServiceReceipt.setImage3(image3);
        aServiceReceipt.setSignatureImage(signatureImage);
        aServiceReceipt.setReceiptDate(currentDate);

        RadioButton requestStatusButton = (RadioButton) findViewById(receiptRequestRadioGroup.getCheckedRadioButtonId());
        aServiceReceipt.setServiceReceipt(requestStatusButton.getText().toString());

        theServiceReceipt = theServiceReceiptDataSource.createServiceReceipt(aServiceReceipt);
        progressDialog = ProgressDialog.show(ServiceReceiptActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }
    public void cancelServiceReceiptView(View view)
    {
        finish();
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {
            apiHelper = new APIHelpers();
            apiHelper.sendServiceReceipt(theServiceReceipt, aUser, getApplicationContext(), handler, saveImagesLocally);
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
            theServiceReceiptDataSource.deleteServiceReceiptById(theServiceReceipt.getId()+"");
        }
    }

    private Handler handler = new Handler() {
        public String responseStatus;
        public String msgTitle, logError;
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            logError = bundle.getString("logError");
            if(msg.what == 1)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(ServiceReceiptActivity.this);
                if(logError != null && logError.length() > 0)
                    alt_bld.setMessage(logError);
                else
                    alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                alert.setTitle(msgTitle);
                alert.show();

                if(!saveImagesLocally)
                    theServiceReceiptDataSource.deleteServiceReceiptById(theServiceReceipt.getId()+"");

                if(progressDialog.isShowing())
                    progressDialog.dismiss();

            }
            else if(msg.what == 0)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(ServiceReceiptActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    } });
                alert.setTitle(msgTitle);
                alert.show();
                theServiceReceiptDataSource.deleteServiceReceiptById(theServiceReceipt.getId()+"");
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

    public void showSignatureImageOptionView(View v)
    {
        Intent mainIntent = new Intent(this, CaptureSignatureMain.class);
        startActivityForResult(mainIntent, REQUEST_SIGNATURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                } else {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
                    alt_bld.setMessage("Request Denied. Please allow acess to camera to take photo.")
                                .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alert.setTitle("Error");
                    alert.show();

                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                }
                else {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
                    alt_bld.setMessage("Request Denied. Please allow acess to Read External Storage to load photo.")
                            .setCancelable(true);
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alert.setTitle("Error");
                    alert.show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public class CustomAdapter extends BaseAdapter {
        JSONArray jsonCustomArray;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(ServiceReceiptActivity serviceReceiptActivity, JSONArray jsonArray) {
            jsonCustomArray = jsonArray;
            context=serviceReceiptActivity;

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

                if(aJSONObj.has("company"))
                    holder.titleTextView.setText(aJSONObj.getString("company"));
                else if(aJSONObj.has("vehicletype"))
                    holder.titleTextView.setText(aJSONObj.getString("vehicletype"));
                else if(aJSONObj.has("policyperiod"))
                    holder.titleTextView.setText(aJSONObj.getString("policyperiod"));

            }
            catch (JSONException exception)
            {

            }
            return convertView;
        }

    }
}
