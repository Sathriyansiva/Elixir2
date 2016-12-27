package com.invicibledevs.elixir;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.invicibledevs.elixir.dao.OTWDataSource;
import com.invicibledevs.elixir.dao.PolicyDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.OTW;
import com.invicibledevs.elixir.model.Policy;
import com.invicibledevs.elixir.model.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotosPolicyActivity extends AppCompatActivity implements Runnable{

    private byte[] image1, image2, image3, image4, image5, image6, image7, image8, image9, image10, image11, image12, image13;
    private byte[] docImage1, docImage2, docImage3, docImage4, docImage5;
    private TextView policyTextView, titleLabel;
    private EditText vehcileNoEditText;
    private LinearLayout headerView, photosHeaderView;
    private Button addImageOptionButton;
    static final int REQUEST_CAMERA = 1, SELECT_FILE = 2;
    private User aUser;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;
    private Policy thePolicy;
    private OTW theOTW;
    private Boolean isCheckVehcileRequest, isSendAllRequest;
    private PolicyDataSource thePolicyDataSource;
    private OTWDataSource theOTWDataSource;
    private String validVehicleNo;
    private String pictureImagePath = "";

    static final int MY_PERMISSIONS_REQUEST_CAMERA = 10, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCheckVehcileRequest = false;
        isSendAllRequest = false;
        setContentView(R.layout.activity_policy_photos);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
        titleLabel = (TextView)findViewById(R.id.titleLabel);

        if(thePolicyDataSource == null)
            thePolicyDataSource = new PolicyDataSource(getApplicationContext());
        if(theOTWDataSource == null)
            theOTWDataSource = new OTWDataSource(getApplicationContext());

        policyTextView = (TextView)findViewById(R.id.policyId_textview);

        vehcileNoEditText = (EditText) findViewById(R.id.editText_vehicle_no);
        headerView = (LinearLayout) findViewById(R.id.headerView);
        photosHeaderView = (LinearLayout) findViewById(R.id.photsHeaderView);

        validVehicleNo = "";
        String policyId = getIntent().getStringExtra("policyId");
        String otwId = getIntent().getStringExtra("otwId");
        boolean isOTW = getIntent().getBooleanExtra("isOTW", false);
        if(!isOTW && policyId == null)
        {
            photosHeaderView.setVisibility(View.VISIBLE);
            headerView.setVisibility(View.GONE);
            thePolicy = new Policy();
        }
        else if(isOTW && otwId == null)
        {
            photosHeaderView.setVisibility(View.VISIBLE);
            headerView.setVisibility(View.GONE);
            theOTW = new OTW();
        }
        else
        {
            photosHeaderView.setVisibility(View.GONE);
            headerView.setVisibility(View.GONE);
            if(!isOTW) {
                thePolicy = thePolicyDataSource.getPolicyById(policyId);
                policyTextView.setText(policyId);
            }
            else {
                theOTW = theOTWDataSource.getOTWById(otwId);
                policyTextView.setText(otwId);
            }

        }


    }

    public void showAddImageOptionView(View view)
    {

        if(photosHeaderView.getVisibility() == View.VISIBLE && validVehicleNo.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Enter Vehicle No.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }

        addImageOptionButton = (Button)view;

        CharSequence[] items1 = { "Take Photo", "Choose from Library", "Cancel" };
        CharSequence[] items2 = { "Send Photo", "Take Photo", "Choose from Library", "Cancel" };
        CharSequence[] shownItems = items1;
        Drawable bitmap = (Drawable)addImageOptionButton.getBackground();


        if(bitmap != null && !(bitmap instanceof ColorDrawable)) {
            shownItems = items2;
        }
        final  CharSequence[] items = shownItems;
        AlertDialog.Builder builder = new AlertDialog.Builder(PhotosPolicyActivity.this);
        builder.setTitle("Add Image!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {

                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(PhotosPolicyActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(PhotosPolicyActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);

                    } else {

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
                        pictureImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ElixirPDF/PolicyIMG-" + System.currentTimeMillis() + ".jpg";
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pictureImagePath)));
//                        Toast.makeText(getApplicationContext(),"camera pass",Toast.LENGTH_LONG).show();
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }

                } else if (items[item].equals("Choose from Library")) {
                    if (ContextCompat.checkSelfPermission(PhotosPolicyActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(PhotosPolicyActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    } else {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(intent, "Select File"),
                                SELECT_FILE);
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                } else if (items[item].equals("Send Photo")) {
                    isSendAllRequest = false;
                    sendImage();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Uri uri = null;

//                File fi = new File("/sdcard/tmp");
                try {
                    uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), pictureImagePath, null, null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap thumbnail = null;
                try {
                    thumbnail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    thumbnail = Elixir.imageOreintationValidator(thumbnail, pictureImagePath);
//                    thumbnail = getResizedBitmap(thumbnail, 1124, 900);
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
                if(this.addImageOptionButton.getId() == R.id.btn_image1) {
                    image1 = bytes.toByteArray();
                    TextView txtView = (TextView)findViewById(R.id.txt_image1);
                    txtView.setVisibility(View.GONE);
                }
                else if(this.addImageOptionButton.getId() == R.id.btn_image2)
                    image2 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image3)
                    image3 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image4)
                    image4 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image5)
                    image5 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image6)
                    image6 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image7)
                    image7 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image8)
                    image8 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image9)
                    image9 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image10)
                    image10 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image11)
                    image11 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image12)
                    image12 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image13)
                    image13 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image1)
                    docImage1 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image2)
                    docImage2 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image3)
                    docImage3 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image4)
                    docImage4 = bytes.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image5)
                    docImage5 = bytes.toByteArray();
                File destination = new File(Environment.getExternalStorageDirectory() + "/ElixirPDF/",System.currentTimeMillis() + ".jpg");

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
                Drawable d = new BitmapDrawable(getResources(), thumbnail);
                this.addImageOptionButton.setBackgroundDrawable(d);
                this.addImageOptionButton.setVisibility(View.VISIBLE);
                this.addImageOptionButton.requestLayout();

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = intent.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                if(selectedImagePath.contains("http://") || selectedImagePath.contains("https://"))
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(PhotosPolicyActivity.this);
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
                else if(this.addImageOptionButton.getId() == R.id.btn_image3)
                    image3 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image4)
                    image4 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image5)
                    image5 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image6)
                    image6 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image7)
                    image7 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image8)
                    image8 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image9)
                    image9 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image10)
                    image10 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image11)
                    image11 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image12)
                    image12 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_image13)
                    image13 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image1)
                    docImage1 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image2)
                    docImage2 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image3)
                    docImage3 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image4)
                    docImage4 = stream.toByteArray();
                else if(this.addImageOptionButton.getId() == R.id.btn_doc_image5)
                    docImage5 = stream.toByteArray();
                Drawable d = new BitmapDrawable(getResources(), bm);
                this.addImageOptionButton.setBackgroundDrawable(d);
                this.addImageOptionButton.setVisibility(View.VISIBLE);
            }
        }
        File file = new File(pictureImagePath);
        file.delete();
    }

//    private Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
//        int[] Req_Dimension;
//        Req_Dimension=resize(image);
//        return Bitmap.createScaledBitmap(image, Req_Dimension[0], Req_Dimension[1], true);
//
////        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
//    }

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

    public void sendImage()
    {
        isCheckVehcileRequest = false;
        if(thePolicy != null) {
            thePolicy.setImage1(image1);
            thePolicy.setImage2(image2);
            thePolicy.setImage3(image3);
            thePolicy.setImage4(image4);
            thePolicy.setImage5(image5);
            thePolicy.setImage6(image6);
            thePolicy.setImage7(image7);
            thePolicy.setImage8(image8);
            thePolicy.setImage9(image9);
            thePolicy.setImage10(image10);
            thePolicy.setImage11(image11);
            thePolicy.setImage12(image12);
            thePolicy.setImage13(image13);
            thePolicy.setDocImage1(docImage1);
            thePolicy.setDocImage2(docImage2);
            thePolicy.setDocImage3(docImage3);
            thePolicy.setDocImage4(docImage4);
            thePolicy.setDocImage5(docImage5);
        }
        else
        {
            theOTW.setImage1(image1);
            theOTW.setImage2(image2);
            theOTW.setImage3(image3);
            theOTW.setImage4(image4);
            theOTW.setImage5(image5);
            theOTW.setImage6(image6);
            theOTW.setImage7(image7);
            theOTW.setImage8(image8);
            theOTW.setImage9(image9);
            theOTW.setImage10(image10);
            theOTW.setImage11(image11);
            theOTW.setImage12(image12);
            theOTW.setImage13(image13);
            theOTW.setDocImage1(docImage1);
            theOTW.setDocImage2(docImage2);
            theOTW.setDocImage3(docImage3);
            theOTW.setDocImage4(docImage4);
            theOTW.setDocImage5(docImage5);
        }
        progressDialog = ProgressDialog.show(PhotosPolicyActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void savePolicyPhotoDetails(View view)
    {
        isSendAllRequest = true;
        sendImage();
    }

    public void cancelPhotoPolicyView(View view)
    {
        if(validVehicleNo.length() > 0 || (thePolicy != null && thePolicy.getPolicyId()!= null)) {
            confirmAndCloseView();
        }
        else if(validVehicleNo.length() > 0 || (theOTW != null && theOTW.getOtwId()!= null)) {
            confirmAndCloseView();
        }
        else {
            if(validVehicleNo.length() == 0)
                setResultOkSoSecondActivityWontBeShown();
            finish();
        }
    }

    @Override
    public void run()
    {
        if(isInternetConnected())
        {

            if(apiHelper == null)
                apiHelper = new APIHelpers();
            if(isCheckVehcileRequest) {
                if(thePolicy != null)
                    apiHelper.checkVehicleNoIsValid(aUser, getApplicationContext(), handler, vehcileNoEditText.getText().toString().toUpperCase(), false);
                else
                    apiHelper.checkVehicleNoIsValid(aUser, getApplicationContext(), handler, vehcileNoEditText.getText().toString().toUpperCase(), true);
            }
            else if(isSendAllRequest) {
                if(thePolicy != null)
                    apiHelper.sendAllPolicyImage(thePolicy, aUser, getApplicationContext(), handler, validVehicleNo);
                else
                    apiHelper.sendAllOTWImage(theOTW, aUser, getApplicationContext(), handler, validVehicleNo);
            }
            else {
                if(thePolicy != null)
                    apiHelper.sendPolicyImage(thePolicy, aUser, getApplicationContext(), handler, addImageOptionButton.getId(), validVehicleNo);
                else
                    apiHelper.sendOTWImage(theOTW, aUser, getApplicationContext(), handler, addImageOptionButton.getId(), validVehicleNo);
            }

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
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(PhotosPolicyActivity.this);
                alt_bld.setMessage(responseStatus)
                        .setCancelable(true);
                AlertDialog alert = alt_bld.create();
                alert.setButton(-1,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!isCheckVehcileRequest) {
                            if(!isSendAllRequest) {
                                addImageOptionButton.setEnabled(false);
                                updateImageViewStatus(addImageOptionButton.getId());
                            }
                            else
                            {
                                updateAllImageViewStatus();
                            }
                        }
                        else
                        {
                            validVehicleNo = vehcileNoEditText.getText().toString().toUpperCase();
                            titleLabel.setText("Send Photo for "+validVehicleNo);
                        }
                    } });
                alert.setTitle(msgTitle);
                alert.show();


            }
            else if(msg.what == 0)
            {
                validVehicleNo = "";
                titleLabel.setText("Send Photo");
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(PhotosPolicyActivity.this);
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
            if((thePolicy != null && thePolicy.getPolicyId()!= null) || (validVehicleNo.length() > 0) || (theOTW != null && theOTW.getOtwId()!= null)) {
                confirmAndCloseView();
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void updateImageViewStatus(int id)
    {
        switch (id)
        {
            case R.id.btn_image1:
            {
                image1 = null;
                break;
            }
            case R.id.btn_image2:
            {
                image2 = null;
                break;
            }
            case R.id.btn_image3:
            {
                image3 = null;
                break;
            }
            case R.id.btn_image4:
            {
                image4 = null;
                break;
            }
            case R.id.btn_image5:
            {
                image5 = null;
                break;
            }
            case R.id.btn_image6:
            {
                image6 = null;
                break;
            }
            case R.id.btn_image7:
            {
                image7 = null;
                break;
            }
            case R.id.btn_image8:
            {
                image8 = null;
                break;
            }
            case R.id.btn_image9:
            {
                image9 = null;
                break;
            }
            case R.id.btn_image10:
            {
                image10 = null;
                break;
            }
            case R.id.btn_image11:
            {
                image11 = null;
                break;
            }
            case R.id.btn_image12:
            {
                image12 = null;
                break;
            }
            case R.id.btn_image13:
            {
                image13 = null;
                break;
            }
            case R.id.btn_doc_image1:
            {
                docImage1 = null;
                break;
            }
            case R.id.btn_doc_image2:
            {
                docImage2 = null;
                break;
            }
            case R.id.btn_doc_image3:
            {
                docImage3 = null;
                break;
            }
            case R.id.btn_doc_image4:
            {
                docImage4 = null;
                break;
            }
            case R.id.btn_doc_image5:
            {
                docImage5 = null;
                break;
            }
        }
    }

    private void updateAllImageViewStatus()
    {
        if(thePolicy != null) {
            if (thePolicy.getImage1() != null) {
                image1 = null;
                findViewById(R.id.btn_image1).setEnabled(false);
            }
            if (thePolicy.getImage2() != null) {
                image2 = null;
                findViewById(R.id.btn_image2).setEnabled(false);
            }
            if (thePolicy.getImage3() != null) {
                image3 = null;
                findViewById(R.id.btn_image3).setEnabled(false);
            }
            if (thePolicy.getImage4() != null) {
                image4 = null;
                findViewById(R.id.btn_image4).setEnabled(false);
            }
            if (thePolicy.getImage5() != null) {
                image5 = null;
                findViewById(R.id.btn_image5).setEnabled(false);
            }
            if (thePolicy.getImage6() != null) {
                image6 = null;
                findViewById(R.id.btn_image6).setEnabled(false);
            }
            if (thePolicy.getImage7() != null) {
                image7 = null;
                findViewById(R.id.btn_image7).setEnabled(false);
            }
            if (thePolicy.getImage8() != null) {
                image8 = null;
                findViewById(R.id.btn_image8).setEnabled(false);
            }
            if (thePolicy.getImage9() != null) {
                image9 = null;
                findViewById(R.id.btn_image9).setEnabled(false);
            }
            if (thePolicy.getImage10() != null) {
                image10 = null;
                findViewById(R.id.btn_image10).setEnabled(false);
            }
            if (thePolicy.getImage11() != null) {
                image11 = null;
                findViewById(R.id.btn_image11).setEnabled(false);
            }
            if (thePolicy.getImage12() != null) {
                image12 = null;
                findViewById(R.id.btn_image12).setEnabled(false);
            }
            if (thePolicy.getImage13() != null) {
                image13 = null;
                findViewById(R.id.btn_image13).setEnabled(false);
            }
            if (thePolicy.getDocImage1() != null) {
                docImage1 = null;
                findViewById(R.id.btn_doc_image1).setEnabled(false);
            }
            if (thePolicy.getDocImage2() != null) {
                docImage2 = null;
                findViewById(R.id.btn_doc_image2).setEnabled(false);
            }
            if (thePolicy.getDocImage3() != null) {
                docImage3 = null;
                findViewById(R.id.btn_doc_image3).setEnabled(false);
            }
            if (thePolicy.getDocImage4() != null) {
                docImage4 = null;
                findViewById(R.id.btn_doc_image4).setEnabled(false);
            }
            if (thePolicy.getDocImage5() != null) {
                docImage5 = null;
                findViewById(R.id.btn_doc_image5).setEnabled(false);
            }
        }
        else
        {
            if (theOTW.getImage1() != null) {
                image1 = null;
                findViewById(R.id.btn_image1).setEnabled(false);
            }
            if (theOTW.getImage2() != null) {
                image2 = null;
                findViewById(R.id.btn_image2).setEnabled(false);
            }
            if (theOTW.getImage3() != null) {
                image3 = null;
                findViewById(R.id.btn_image3).setEnabled(false);
            }
            if (theOTW.getImage4() != null) {
                image4 = null;
                findViewById(R.id.btn_image4).setEnabled(false);
            }
            if (theOTW.getImage5() != null) {
                image5 = null;
                findViewById(R.id.btn_image5).setEnabled(false);
            }
            if (theOTW.getImage6() != null) {
                image6 = null;
                findViewById(R.id.btn_image6).setEnabled(false);
            }
            if (theOTW.getImage7() != null) {
                image7 = null;
                findViewById(R.id.btn_image7).setEnabled(false);
            }
            if (theOTW.getImage8() != null) {
                image8 = null;
                findViewById(R.id.btn_image8).setEnabled(false);
            }
            if (theOTW.getImage9() != null) {
                image9 = null;
                findViewById(R.id.btn_image9).setEnabled(false);
            }
            if (theOTW.getImage10() != null) {
                image10 = null;
                findViewById(R.id.btn_image10).setEnabled(false);
            }
            if (theOTW.getImage11() != null) {
                image11 = null;
                findViewById(R.id.btn_image11).setEnabled(false);
            }
            if (theOTW.getImage12() != null) {
                image12 = null;
                findViewById(R.id.btn_image12).setEnabled(false);
            }
            if (theOTW.getImage13() != null) {
                image13 = null;
                findViewById(R.id.btn_image13).setEnabled(false);
            }
            if (theOTW.getDocImage1() != null) {
                docImage1 = null;
                findViewById(R.id.btn_doc_image1).setEnabled(false);
            }
            if (theOTW.getDocImage2() != null) {
                docImage2 = null;
                findViewById(R.id.btn_doc_image2).setEnabled(false);
            }
            if (theOTW.getDocImage3() != null) {
                docImage3 = null;
                findViewById(R.id.btn_doc_image3).setEnabled(false);
            }
            if (theOTW.getDocImage4() != null) {
                docImage4 = null;
                findViewById(R.id.btn_doc_image4).setEnabled(false);
            }
            if (theOTW.getDocImage5() != null) {
                docImage5 = null;
                findViewById(R.id.btn_doc_image5).setEnabled(false);
            }
            confirmAndGoBackView();
        }
    }
    public void confirmAndCloseView()
    {

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("If you haven't send single photo then your policy not shown in office. Do you want to Continue?")
                .setCancelable(true);
        AlertDialog alert = alt_bld.create();
        alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(validVehicleNo.length() == 0)
                    setResultOkSoSecondActivityWontBeShown();
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

    public void confirmAndGoBackView()
    {

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("Do you want to Continue?")
                .setCancelable(true);
        AlertDialog alert = alt_bld.create();
        alert.setButton(-1, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(validVehicleNo.length() == 0)
                    setResultOkSoSecondActivityWontBeShown();
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
                    alt_bld.setMessage("Request Denied. Please allow acessto camera to take photo.")
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
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

    private void setResultOkSoSecondActivityWontBeShown() {
        Intent intent = new Intent();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
    }

    public void checkVehcileNo(View view)
    {
        String vehicleNo = vehcileNoEditText.getText().toString().trim();
        if(vehicleNo.equals(""))
        {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage("Enter Vehicle No.")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alert.setTitle("Error");
            alert.show();
            return;
        }
        isCheckVehcileRequest = true;
        progressDialog = ProgressDialog.show(PhotosPolicyActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }
}
