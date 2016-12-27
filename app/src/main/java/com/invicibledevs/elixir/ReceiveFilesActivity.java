package com.invicibledevs.elixir;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.helper.APIHelpers;
import com.invicibledevs.elixir.helper.Elixir;
import com.invicibledevs.elixir.model.ReceiveFile;
import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

public class ReceiveFilesActivity extends AppCompatActivity implements Runnable {

    private ListView listView;
    private TextView noListTextView;
    private ProgressDialog progressDialog;
    private APIHelpers apiHelper;
    private ArrayList<ReceiveFile> fileArrayList;
    private User aUser;
    private ReceiveFile aFileObj;
    private int fileIndex, startIndex;
    private boolean isFileDownloadRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_files);
        Elixir.setUpdatedReceiveFilesList(null);
        listView = (ListView) findViewById(R.id.files_list);
        noListTextView = (TextView) findViewById(R.id.no_files_label);

        fileArrayList = new ArrayList<ReceiveFile>();

        if(fileArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noListTextView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(new CustomAdapter(this, fileArrayList));
        UserDataSource theUserDataSource = new UserDataSource(getApplicationContext());
        SharedPreferences elixirPreferences = getApplicationContext().getSharedPreferences("ELIXIRPreferences", getApplicationContext().MODE_PRIVATE);
        aUser = theUserDataSource.getUserByUserId(elixirPreferences.getString("loggedInUserId", ""));
        refreshFilesList(null);
    }

    public class CustomAdapter extends BaseAdapter {
        ArrayList<ReceiveFile> fileArrayList;
        Context context;

        private LayoutInflater inflater=null;
        public CustomAdapter(ReceiveFilesActivity receiveFilesListActivity, ArrayList<ReceiveFile> otwList) {
            fileArrayList=otwList;
            context=receiveFilesListActivity;

            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setUpdateDataList(ArrayList<ReceiveFile> otwList)
        {
            fileArrayList=otwList;
        }
        @Override
        public int getCount() {
            if(fileArrayList == null)
                return 0;
            return fileArrayList.size();
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

                convertView = inflater.inflate(R.layout.activity_receivefiles_listview, null);
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

            ReceiveFile aFile = fileArrayList.get(position);
            holder.customerName.setText("Name: " + aFile.getSenderName());
            holder.otwId.setText("Date: " + aFile.getDate());
            holder.status.setText("Message: " + aFile.getMessage());

            if(aFile.isDownloaded()) {
                holder.deleteButton.setVisibility(View.GONE);
            }
            else {
                holder.deleteButton.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

    }


    public void downloadListItem(View view)
    {
        aFileObj = fileArrayList.get((int)view.getTag());
        fileIndex = (int)view.getTag();
        downloadFile();
    }

    public void refreshFilesList(View view)
    {
        startIndex = 1;
        isFileDownloadRequest = false;
        progressDialog = ProgressDialog.show(ReceiveFilesActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void loadOldFiles(View view)
    {
        if(fileArrayList != null)
            startIndex = startIndex+fileArrayList.size();
        isFileDownloadRequest = false;
        progressDialog = ProgressDialog.show(ReceiveFilesActivity.this,
                "", "Please wait...", true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void downloadFile()
    {
        isFileDownloadRequest = true;
        progressDialog = ProgressDialog.show(ReceiveFilesActivity.this,
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
            if(isFileDownloadRequest)
                apiHelper.downloadFile(aUser, aFileObj, getApplicationContext(), handler);
            else
                apiHelper.getFilesList(aUser, getApplicationContext(), handler, startIndex);
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
        public String msgTitle, logError;

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            responseStatus = bundle.getString("responseStatus");
            msgTitle = bundle.getString("msgTitle");
            if(isFileDownloadRequest)
            {
                logError = bundle.getString("logError");
                if(msg.what == 1)
                {
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(ReceiveFilesActivity.this);
                    if(logError != null && logError.length() > 0) {
                        if(logError.contains("ErrorMSG")) {
                            alt_bld.setMessage(logError.replace("ErrorMSG",""));
                        }
                        else
                        {
                            alt_bld.setMessage(logError);
                            aFileObj.setIsDownloaded(true);
                            aFileObj.setDownloadedPath(logError);
                            fileArrayList.remove(fileIndex);
                            fileArrayList.add(fileIndex, aFileObj);
                            Elixir.setUpdatedReceiveFilesList(fileArrayList);
                            reloadListView();
                        }
                    }
                    else {

                        alt_bld.setMessage(responseStatus)
                                .setCancelable(true);
                    }
                    AlertDialog alert = alt_bld.create();
                    alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alert.setTitle(msgTitle);
                    alert.show();

                }
                else if(msg.what == 0)
                {
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(ReceiveFilesActivity.this);
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
                if (msg.what == 1) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    reloadListView();

                } else if (msg.what == 0) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(ReceiveFilesActivity.this);
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

    private void reloadListView() {
        fileArrayList = Elixir.getReceiveFilesList();
        if(fileArrayList == null || fileArrayList.size() == 0)
        {
            listView.setVisibility(View.GONE);
            noListTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            listView.setVisibility(View.VISIBLE);
            noListTextView.setVisibility(View.GONE);
        }
        CustomAdapter customAdapter = (CustomAdapter) listView.getAdapter();
        customAdapter.setUpdateDataList(fileArrayList);
        customAdapter.notifyDataSetChanged();
        listView.invalidateViews();
        listView.scrollBy(0, 0);
    }

}
