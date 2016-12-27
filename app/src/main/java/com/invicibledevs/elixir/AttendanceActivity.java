package com.invicibledevs.elixir;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.invicibledevs.elixir.dao.AttendanceDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.DecimalDigitsInputFilter;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.helper.LocationService;
import com.invicibledevs.elixir.model.Attendance;
import com.invicibledevs.elixir.model.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AttendanceActivity extends AppCompatActivity implements Runnable {

    static final int REQUEST_CAMERA = 1, SELECT_FILE = 2;
    private ImageButton imageButton;
    private Button addImageButton;
    private RadioGroup attendanceRadioGroup;
    private AttendanceDataSource aAttendanceDataSource;
    private EditText balanceAmountTextField, twCountTextField, twCMSAmountTextField, twServiceChargeTextField;
    private byte[] attendanceImage;

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;
    private APIHelpers apiHelper;
    private ProgressDialog progressDialog;
    private Attendance theAttendance;
    private User aUser;
    private boolean isGetOutTimeRequest;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 10, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;
    private String pictureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));

        imageButton = (ImageButton) findViewById(R.id.btn_image);
        addImageButton = (Button) findViewById(R.id.btn_add_image);
        attendanceRadioGroup = (RadioGroup) findViewById(R.id.timeRadioGroup);

        balanceAmountTextField = (EditText) findViewById(R.id.editText_balance_amount);
        balanceAmountTextField.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(9, 2)});
        twCountTextField = (EditText) findViewById(R.id.editText_tw_count);
        twCMSAmountTextField = (EditText) findViewById(R.id.editText_tw_cms_amount);
        twServiceChargeTextField = (EditText) findViewById(R.id.editText_tw_service_charge);
        int checkedRadioButtonID = attendanceRadioGroup.getCheckedRadioButtonId();
        attendanceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup arg0, int id) {
                switch (id) {
                    case R.id.in_time: {
                        LinearLayout inTimeView = (LinearLayout) findViewById(R.id.in_time_view);
                        LinearLayout outTimeView = (LinearLayout) findViewById(R.id.out_time_view);
                        inTimeView.setVisibility(View.VISIBLE);
                        outTimeView.setVisibility(View.GONE);
                    }
                    break;
                    case R.id.out_time: {

                        LinearLayout inTimeView = (LinearLayout) findViewById(R.id.in_time_view);
                        LinearLayout outTimeView = (LinearLayout) findViewById(R.id.out_time_view);
                        inTimeView.setVisibility(View.GONE);
                        outTimeView.setVisibility(View.VISIBLE);
                        if (twCountTextField.getText().toString().trim().length() == 0) {
                            isGetOutTimeRequest = true;
                            progressDialog = ProgressDialog.show(AttendanceActivity.this,
                                    "", "Please wait...", true);

                            Thread thread = new Thread(AttendanceActivity.this);
                            thread.start();
                        }
                    }
                    break;

                    default:

                        break;
                }
            }
        });

        if (aAttendanceDataSource == null)
            aAttendanceDataSource = new AttendanceDataSource(getApplicationContext());

        dateView = (TextView) findViewById(R.id.textview_lock_date);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

    }

    public void showAddImageOptionView(View view) {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AttendanceActivity.this);
        builder.setTitle("Add Image!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(AttendanceActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(AttendanceActivity.this,
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
                        pictureImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ElixirPDF/AT-" + System.currentTimeMillis() + ".jpg";
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pictureImagePath)));
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    if (ContextCompat.checkSelfPermission(AttendanceActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(AttendanceActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    } else {
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
            if (requestCode == REQUEST_CAMERA) {
//                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

                Uri uri = null;

                try {
                    uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), pictureImagePath, null, null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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
                attendanceImage = bytes.toByteArray();
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

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

                this.imageButton.setImageBitmap(thumbnail);
                this.imageButton.setVisibility(View.VISIBLE);
                this.addImageButton.setVisibility(View.GONE);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaColumns.DATA};
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                if(selectedImagePath.contains("http://") || selectedImagePath.contains("https://"))
                {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(AttendanceActivity.this);
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
                attendanceImage = stream.toByteArray();
                this.imageButton.setImageBitmap(bm);
                this.imageButton.setVisibility(View.VISIBLE);
                this.addImageButton.setVisibility(View.GONE);
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

    public void saveAttendanceDetails(View view) {
        LocationService aLocationService = LocationService.getLocationManager(getApplicationContext());
//        aLocationService.stopUpdates(getApplicationContext());
        aLocationService.initLocationService(getApplicationContext());
        Location currentLocation = aLocationService.location;
        float[] results = new float[1];
        if (currentLocation == null) {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(AttendanceActivity.this);
            alt_bld.setMessage("Please enable GPS service in settings to do attendance")
                    .setCancelable(true);
            AlertDialog alert = alt_bld.create();
            alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alert.setTitle("Error");
            alert.show();
            return;
        } else if (aUser.getLatitude() == null || aUser.getLongitude() == null || aUser.getLatitude().length() == 0 || aUser.getLongitude().length() == 0) {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(AttendanceActivity.this);
            alt_bld.setMessage("Please contact admin since your're not mapped to location.")
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
        Location.distanceBetween(Double.parseDouble(aUser.getLatitude()), Double.parseDouble(aUser.getLongitude()), currentLocation.getLatitude(), currentLocation.getLongitude(), results);
//        Location.distanceBetween(Double.parseDouble(aUser.getLatitude()), Double.parseDouble(aUser.getLongitude()), 13.088618, 80.284861, results);

        if (results[0] > 400) {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(AttendanceActivity.this);
            alt_bld.setMessage("Please visit the work location to do attendance")
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

        isGetOutTimeRequest = false;
        int checkedRadioButtonID = attendanceRadioGroup.getCheckedRadioButtonId();
        Attendance aAttendance = new Attendance();
        if (checkedRadioButtonID == R.id.in_time) {
            String lockDateString = dateView.getText().toString().trim();
            String balanceAmount = balanceAmountTextField.getText().toString().trim();
            if (lockDateString.equals("") || balanceAmount.equals("") || attendanceImage == null) {
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
            } else {
                Date lockDate = getDateFromString("dd/MM/yyyy", dateView.getText().toString());
                aAttendance.setLockDateString(dateView.getText().toString());
                aAttendance.setLockDate(lockDate);
                aAttendance.setBalanceAmount(Double.parseDouble(this.balanceAmountTextField.getText().toString()));
                aAttendance.setIsInTime(Boolean.TRUE);
                aAttendance.setImage(attendanceImage);
            }
        } else {
            String twCount = twCountTextField.getText().toString().trim();
            String cmsAmt = twCMSAmountTextField.getText().toString().trim();
            String serviceCharge = twServiceChargeTextField.getText().toString().trim();
            if (twCount.equals("") || cmsAmt.equals("") || serviceCharge.equals("")) {
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
            } else {
                aAttendance.setTwCount(Integer.parseInt(this.twCountTextField.getText().toString()));
                aAttendance.setTwCMSAmount(Double.parseDouble(this.twCMSAmountTextField.getText().toString()));
                aAttendance.setTwServiceCharge(Double.parseDouble(this.twServiceChargeTextField.getText().toString()));
                aAttendance.setIsInTime(Boolean.FALSE);
            }
        }
        theAttendance = aAttendance;
//        aAttendanceDataSource.createAttendance(aAttendance);

        progressDialog = ProgressDialog.show(AttendanceActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    private Date getDateFromString(String formate, String dateString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(formate);
            Date lockDate = formatter.parse(dateString);
            return lockDate;
        } catch (ParseException e) {
            return null;
        }
    }

    public void cancelAttendanceView(View view) {
        finish();
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.aAttendanceActivity = this;
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public AttendanceActivity aAttendanceActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            int year = 0;
            int month = 0;
            int day = 0;
            Date lockDate = aAttendanceActivity.getDateFromString("dd/MM/yyyy", aAttendanceActivity.getLockDateString());
            if (lockDate != null) {
                final Calendar c = Calendar.getInstance();
                c.setTime(lockDate);
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
            dp.setMinDate(timeInMills);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            aAttendanceActivity.showDate(year, month + 1, day);
        }


    }


    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String getLockDateString() {
        return dateView.getText().toString();
    }

    @Override
    public void run() {
        if (isInternetConnected()) {
            apiHelper = new APIHelpers();
            if (isGetOutTimeRequest) {
                apiHelper.getOutTimeServiceAttendance(aUser, getApplicationContext(), handler);
            } else {
                if (theAttendance.isInTime())
                    apiHelper.sendInTimeAttendance(theAttendance, aUser, getApplicationContext(), handler);
                else
                    apiHelper.sendOutTimeAttendance(theAttendance, aUser, getApplicationContext(), handler);
            }
        } else {
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
            if (msg.what == 1) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                if (isGetOutTimeRequest) {
                    twCountTextField.setText(bundle.getString("twcount"));
                    twCMSAmountTextField.setText(bundle.getString("cmsamount"));
                    twServiceChargeTextField.setText(bundle.getString("servicecharge"));
                } else {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(AttendanceActivity.this);
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
                }

            } else if (msg.what == 0) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(AttendanceActivity.this);
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
    };

    /**
     * Returns the internet connection status
     *
     * @return
     */
    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isAvailable() && ni.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
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
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else {
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
        }
    }
}
