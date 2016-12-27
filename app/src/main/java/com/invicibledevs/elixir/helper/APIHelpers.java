package com.invicibledevs.elixir.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.invicibledevs.elixir.R;
import com.invicibledevs.elixir.dao.CMSListDataSource;
import com.invicibledevs.elixir.dao.OTWDataSource;
import com.invicibledevs.elixir.dao.PolicyDataSource;
import com.invicibledevs.elixir.dao.UserDataSource;
import com.invicibledevs.elixir.model.AgentPayment;
import com.invicibledevs.elixir.model.Attendance;
import com.invicibledevs.elixir.model.CMS;
import com.invicibledevs.elixir.model.CMSList;
import com.invicibledevs.elixir.model.ExecutiveReport;
import com.invicibledevs.elixir.model.Leads;
import com.invicibledevs.elixir.model.OTW;
import com.invicibledevs.elixir.model.Payment;
import com.invicibledevs.elixir.model.PaymentEntry;
import com.invicibledevs.elixir.model.PaymentReport;
import com.invicibledevs.elixir.model.PaymentTypeDetails;
import com.invicibledevs.elixir.model.Policy;
import com.invicibledevs.elixir.model.ReceiveFile;
import com.invicibledevs.elixir.model.Role;
import com.invicibledevs.elixir.model.ServiceReceipt;
import com.invicibledevs.elixir.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by nandhakumarv on 29/11/15.
 */
public class APIHelpers {

    private Context context;
    private String API_ENDPOINT_PROD = "http://122.165.112.126:3037/";
    private String SYNC_API_ENDPO_SYNC_PROD = "http://122.165.112.126:90/";
    private UserDataSource theUserDataSource;
    private OTWDataSource theOTWDataSource;
    private CMSListDataSource theCMSListDataSource;
    private PolicyDataSource thePolicyDataSource;

    public enum CMSAmountType {
        CMS(0),
        SERVICE(1),
        NONCMS(2);

        private final int value;

        private CMSAmountType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public void authenticateUser(String username, String password, String imeiNumber, Context context, Handler handler)
    {
        try
        {
            SharedPreferences elixirPreferences = context.getSharedPreferences("ELIXIRPreferences", context.MODE_PRIVATE);
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("username", username);
            requestJson.put("password", password);
            requestJson.put("imei", imeiNumber);

            URL url = new URL(API_ENDPOINT_PROD + "api/login");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();
            SharedPreferences.Editor prefEditor = elixirPreferences.edit();
            JSONObject aJsonObj = new JSONObject(responseString);
            boolean validUser = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(validUser)
            {
                if(theUserDataSource == null)
                    theUserDataSource = new UserDataSource(context);
                User aUser = new User();
                aUser.setUserId((Integer) aJsonObj.get("userid") + "");

                prefEditor.putString("loggedInUserId", aUser.getUserId());
                prefEditor.putString("areaName", (String) aJsonObj.getString("locationname"));
                prefEditor.putString("rolename", (String) aJsonObj.getString("rolename"));
                prefEditor.putString("permissionStatus", (String) aJsonObj.getString("permission"));
                prefEditor.putString("appversion", (String) aJsonObj.getString("versionname"));
                prefEditor.commit();

                if(!aJsonObj.getString("branchid").equalsIgnoreCase("null"))
                    aUser.setBranchId( aJsonObj.getString("branchid") + "");
                else
                    aUser.setBranchId("");
                if(!aJsonObj.getString("locationid").equalsIgnoreCase("null") )
                    aUser.setLocationId( aJsonObj.getString("locationid") + "");
                else
                    aUser.setLocationId("");
                if(!aJsonObj.getString("teamleaderid").equalsIgnoreCase("null"))
                    aUser.setTeamLeaderId(aJsonObj.getString("teamleaderid") + "");
                else
                    aUser.setTeamLeaderId("");
                aUser.setLocationName((String) aJsonObj.getString("locationname"));
                aUser.setAreaName((String) aJsonObj.getString("areaname"));
                aUser.setExecutiveName((String) aJsonObj.getString("Executivename"));
                aUser.setGeneralNews((String) aJsonObj.getString("generalnews"));
                aUser.setPersonalNews((String) aJsonObj.getString("personalnews"));
                aUser.setLatitude((String) aJsonObj.getString("latitude"));
                aUser.setLongitude((String) aJsonObj.getString("longitude"));
                aUser.setServiceCharge((String) aJsonObj.getString("servicecharge"));
                aUser.setDetails((String) aJsonObj.getString("details"));
                if(!aJsonObj.getString("roleid").equalsIgnoreCase("null"))
                    aUser.setRoleId((String) aJsonObj.getString("roleid"));
                else
                    aUser.setRoleId("");

                theUserDataSource.deleteUserByUserId(aUser.getUserId());
                theUserDataSource.createUser(aUser);

                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("appversion", (String) aJsonObj.get("versionname"));
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                if(aJsonObj.has("message"))
                    mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                else
                    mBundle.putString("responseStatus", "Your username or password was not correct. Please try again.");
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendIMEINumber(String imeiNumber, Context context, Handler handler, Location currentLocation)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("imei", imeiNumber);
            requestJson.put("latitude", currentLocation.getLatitude() + "");
            requestJson.put("longitude", currentLocation.getLongitude()+"");

            URL url = new URL(API_ENDPOINT_PROD + "api/key");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                handler.sendEmptyMessage(1);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                if(aJsonObj.has("message"))
                    mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                else
                    mBundle.putString("responseStatus", "Unable to send IMEI number. Please try again.");
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendInTimeAttendance(Attendance aAttendance, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId()+"");
            requestJson.put("locationid", aUser.getLocationId()+"");
            requestJson.put("tlid", aUser.getTeamLeaderId()+"");
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("lockdate", aAttendance.getLockDateString());
            requestJson.put("balance", aAttendance.getBalanceAmount()+"");
            String encodedImage = Base64.encodeToString(aAttendance.getImage(), Base64.DEFAULT);
            requestJson.put("image", encodedImage);


            URL url = new URL(API_ENDPOINT_PROD + "api/intime");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getOutTimeServiceAttendance( User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("tlid", aUser.getTeamLeaderId()+"");
            URL url = new URL(API_ENDPOINT_PROD + "api/outtime");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {

                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("twcount", aJsonObj.get("twcount").toString());
                mBundle.putString("cmsamount", aJsonObj.get("CMSamount").toString());
                mBundle.putString("servicecharge", aJsonObj.get("servicecharge").toString());

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendOutTimeAttendance(Attendance aAttendance, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("twcount", aAttendance.getTwCount()+"");
            requestJson.put("CMSamount", aAttendance.getTwCMSAmount()+"");
            requestJson.put("servicecharge", aAttendance.getTwServiceCharge()+"");
            requestJson.put("tlid", aUser.getTeamLeaderId()+"");

            URL url = new URL(API_ENDPOINT_PROD + "api/outtime1");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendServiceReceipt(ServiceReceipt aServiceReceipt, User aUser, Context context, Handler handler, boolean saveImagesLocally)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId()+"");
            requestJson.put("locationid", aUser.getLocationId()+"");
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("tlid", aUser.getTeamLeaderId()+"");
            requestJson.put("policyno", aServiceReceipt.getPolicyNo());
            requestJson.put("policyperiod", aServiceReceipt.getPolicyPeriod());
            requestJson.put("name", aServiceReceipt.getName());
            requestJson.put("mobileno", aServiceReceipt.getMobileNo());
            requestJson.put("vehicleno", aServiceReceipt.getVehicleNo().toUpperCase());
            requestJson.put("vehicletype", aServiceReceipt.getVehicleType());
            requestJson.put("policyamt", aServiceReceipt.getPolicyAmount()+"");
            requestJson.put("od", aServiceReceipt.getOd()+"");
            requestJson.put("service", aServiceReceipt.getServiceCharge()+"");
            requestJson.put("emailid", aServiceReceipt.getEmail());
            requestJson.put("companyname", aServiceReceipt.getCompanyName());
            if(saveImagesLocally)
            {
                requestJson.put("image", "");
                requestJson.put("image1", "");
                requestJson.put("image2", "");
                requestJson.put("signature_image", "");
            }
            else {
                String encodedImage = Base64.encodeToString(aServiceReceipt.getImage1(), Base64.DEFAULT);
                requestJson.put("image", encodedImage);
                encodedImage = Base64.encodeToString(aServiceReceipt.getImage2(), Base64.DEFAULT);
                requestJson.put("image1", encodedImage);
                encodedImage = Base64.encodeToString(aServiceReceipt.getImage3(), Base64.DEFAULT);
                requestJson.put("image2", encodedImage);
                encodedImage = Base64.encodeToString(aServiceReceipt.getSignatureImage(), Base64.DEFAULT);
                requestJson.put("signature_image", encodedImage);
            }
            requestJson.put("servicereceipt", aServiceReceipt.getServiceReceipt().toLowerCase());


            URL url = new URL(API_ENDPOINT_PROD + "api/service");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                String pdfEncoded = aJsonObj.getString("base64");
                if (!pdfEncoded.equalsIgnoreCase("0"))
                {
                    byte[] decodedPdf = Base64.decode(pdfEncoded, Base64.DEFAULT);
                    mBundle.putByteArray("service_receipt_pdf", decodedPdf);
                    String res = writeToSDFile(aServiceReceipt,decodedPdf);
                    mBundle.putString("logError", res);
                }


                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (Exception e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendServiceReceiptImages(ServiceReceipt aServiceReceipt, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("date", aServiceReceipt.getReceiptDate());
            requestJson.put("vehicleno", aServiceReceipt.getVehicleNo().toUpperCase());

                String encodedImage = Base64.encodeToString(aServiceReceipt.getImage1(), Base64.DEFAULT);
                requestJson.put("image", encodedImage);
                encodedImage = Base64.encodeToString(aServiceReceipt.getImage2(), Base64.DEFAULT);
                requestJson.put("image1", encodedImage);
                encodedImage = Base64.encodeToString(aServiceReceipt.getImage3(), Base64.DEFAULT);
                requestJson.put("image2", encodedImage);
                encodedImage = Base64.encodeToString(aServiceReceipt.getSignatureImage(), Base64.DEFAULT);
                requestJson.put("signature_image", encodedImage);


            URL url = new URL(SYNC_API_ENDPO_SYNC_PROD + "api/sync");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();


                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (Exception e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendLead(Leads aLead, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId()+"");
            requestJson.put("locationid", aUser.getLocationId()+"");
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("tlid", aUser.getTeamLeaderId()+"");
            requestJson.put("name", aLead.getName());
            requestJson.put("vehicletype", aLead.getVehicleType());
            requestJson.put("vehicleno", aLead.getVehicleNo().toUpperCase());
            requestJson.put("mobile", aLead.getMobileNo());
            requestJson.put("riskend", aLead.getRiskEndDateString());


            requestJson.put("preinsurance", aLead.getPreviousInsurance());
            requestJson.put("ncb", aLead.getNcb());
            requestJson.put("idv", aLead.getIdv()+"");
            requestJson.put("email", aLead.getEmail());
            requestJson.put("yearofmanu", aLead.getYom());
            requestJson.put("make", aLead.getMake());
            requestJson.put("model", aLead.getModel());


            URL url = new URL(API_ENDPOINT_PROD + "api/leads");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendOTW(OTW aOTW, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId()+"");
            requestJson.put("locateid", aUser.getLocationId()+"");
            requestJson.put("tlid", aUser.getTeamLeaderId());
            requestJson.put("userid", aUser.getUserId());

            requestJson.put("name", aOTW.getCustomerName());
            requestJson.put("address", aOTW.getAddress());
            requestJson.put("mobile", aOTW.getMobileNo());
            requestJson.put("companyname", aOTW.getCompanyName());
            requestJson.put("vehicletype", aOTW.getVehicleType());
            requestJson.put("vehicleno", aOTW.getVehicleNo().toUpperCase());
            requestJson.put("engineno", aOTW.getEngineNo().toUpperCase());
            requestJson.put("chasisno", aOTW.getChasisNo().toUpperCase());
            requestJson.put("Dateofreg", aOTW.getDateOfReg());
            requestJson.put("make", aOTW.getMake());
            requestJson.put("model", aOTW.getModel());
            requestJson.put("idv", aOTW.getIdv()+"");
            requestJson.put("cc", aOTW.getCc());
            requestJson.put("previousinsurance", aOTW.isHavingPreviousInsurance()?"yes":"no");
            requestJson.put("ncb", aOTW.getNcb());
            requestJson.put("previouscompany", aOTW.getPreviousInsurance()+"");
            requestJson.put("od", aOTW.getOdDiscount()+"");
            requestJson.put("premiumamt", aOTW.getPreminumAmount()+"");
            requestJson.put("servicecharge", aOTW.getServiceCharege()+"");
            requestJson.put("riskdate", aOTW.getRiskStartDate());
            requestJson.put("finstatus", aOTW.isHavingFinance()?"yes":"no");
            requestJson.put("finname", aOTW.getFinancierName());

            requestJson.put("paymode", aOTW.getPaymentType());
            requestJson.put("policytype", aOTW.getPolicyType());

            requestJson.put("imt23", aOTW.isImtStatus()?"yes":"no");
            requestJson.put("niltip", aOTW.isNilDIPStatus()?"yes":"no");
            requestJson.put("year", aOTW.getYearOfManufacture());
            requestJson.put("claims", aOTW.isClaimsStatus()?"yes":"no");
            requestJson.put("nametransfer", aOTW.isNameTransferStatus()?"yes":"no");
            requestJson.put("previousno", aOTW.getPreviousInsuranceNo());
            requestJson.put("email", aOTW.getEmailId());
            requestJson.put("vehiclecolor", aOTW.getVehicleColor());
            requestJson.put("pincode", aOTW.getPinCode());

            URL url = new URL(API_ENDPOINT_PROD + "api/Otw_Addpolicy");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                mBundle.putString("otId", (String)aJsonObj.get("policyid"));
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getOTWStatus(ArrayList<OTW> otwList, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            if(theOTWDataSource == null)
                theOTWDataSource = new OTWDataSource(context);
            String otwIds = "";
            for(Iterator<OTW> i = otwList.iterator();i.hasNext();)
            {
                OTW aOtw = i.next();
                if(aOtw.getOtwId() == null || aOtw.getOtwId().length() == 0)
                    continue;
                if(aOtw.getStatus() != null && !aOtw.getStatus().equalsIgnoreCase("Fresh"))
                    continue;;
                if (otwIds.length() == 0)
                    otwIds = aOtw.getOtwId();
                else
                    otwIds = otwIds + "," + aOtw.getOtwId();
            }
            JSONObject requestJson = new JSONObject();
            requestJson.put("policyids", otwIds);


            URL url = new URL(API_ENDPOINT_PROD + "api/Otw_Policypdf");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray otwJsonList = aJsonObj.getJSONArray("eList");

            for (int i =0; i<otwJsonList.length();i++)
            {
                JSONObject otwObject = otwJsonList.getJSONObject(i);
                if(otwObject.getString("id").length()>0) {
                    OTW aOTW = theOTWDataSource.getOTWById(otwObject.getString("id"));
                    aOTW.setStatus(otwObject.getString("status"));

                    String pdfEncoded = otwObject.getString("pdf");
                    if (pdfEncoded.equalsIgnoreCase("0"))
                        aOTW.setPdfPath("");
                    else {
                        byte[] decodedPdf = Base64.decode(pdfEncoded, Base64.DEFAULT);
                        aOTW.setPdfPath(writeToSDFile(aOTW.getOtwId()+"_otw", decodedPdf, false));
                    }
                    theOTWDataSource.updateOTW(aOTW);
                }

            }
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getPaymentEntryDetails(User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;

            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId());


            URL url = new URL(API_ENDPOINT_PROD + "api/exe");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray paymentJsonList = aJsonObj.getJSONArray("eList");
            ArrayList<Payment> paymentList = new ArrayList<>();

            for (int i =0; i<paymentJsonList.length();i++)
            {
                JSONObject paymentEntryObject = paymentJsonList.getJSONObject(i);
                if(paymentEntryObject.getString("id").length()>0) {
                    Payment aPayment = new Payment();
                    aPayment.setId(paymentEntryObject.getString("id"));
                    aPayment.setName(paymentEntryObject.getString("name"));
                    if(!paymentEntryObject.getString("totalcms").equalsIgnoreCase("null"))
                        aPayment.setCms(paymentEntryObject.getDouble("totalcms"));
                    if(!paymentEntryObject.getString("totalservice").equalsIgnoreCase("null"))
                        aPayment.setService(paymentEntryObject.getDouble("totalservice"));
                    if(!paymentEntryObject.getString("totalotw").equalsIgnoreCase("null"))
                        aPayment.setOtw(paymentEntryObject.getDouble("totalotw"));
                    JSONArray cmsJsonList = paymentEntryObject.getJSONArray("cmslist");
                    ArrayList<PaymentTypeDetails> aCMSList = new ArrayList<PaymentTypeDetails>();
                    for (int j =0; j<cmsJsonList.length();j++)
                    {
                        JSONObject cmsJSONObject = cmsJsonList.getJSONObject(j);
                        PaymentTypeDetails aCMSObject = new PaymentTypeDetails();
                        aCMSObject.setPaymentDate(cmsJSONObject.getString("cmsdate"));
                        aCMSObject.setPaymentAmount(cmsJSONObject.getDouble("cmsamount"));
                        aCMSList.add(aCMSObject);
                    }
                    aPayment.setCmsList(aCMSList);
                    JSONArray serviceJsonList = paymentEntryObject.getJSONArray("servicelist");
                    ArrayList<PaymentTypeDetails> aServiceList = new ArrayList<PaymentTypeDetails>();
                    for (int k =0; k<serviceJsonList.length();k++)
                    {
                        JSONObject serviceJSONObject = serviceJsonList.getJSONObject(k);
                        PaymentTypeDetails aServiceObject = new PaymentTypeDetails();
                        aServiceObject.setPaymentDate(serviceJSONObject.getString("servicedatee"));
                        aServiceObject.setPaymentAmount(serviceJSONObject.getDouble("serviceamount"));
                        aServiceList.add(aServiceObject);
                    }
                    aPayment.setServiceList(aServiceList);

                    JSONArray otwJsonList = paymentEntryObject.getJSONArray("otwlist");
                    ArrayList<PaymentTypeDetails> aOTWList = new ArrayList<PaymentTypeDetails>();
                    for (int l =0; l<otwJsonList.length();l++)
                    {
                        JSONObject otwJSONObject = otwJsonList.getJSONObject(l);
                        PaymentTypeDetails aOTWObject = new PaymentTypeDetails();
                        aOTWObject.setPaymentDate(otwJSONObject.getString("otwdatee"));
                        aOTWObject.setPaymentAmount(otwJSONObject.getDouble("otwamount"));
                        aOTWList.add(aOTWObject);
                    }
                    aPayment.setOtwList(aOTWList);
                    paymentList.add(aPayment);
                }

            }
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            Elixir.setPaymentList(paymentList);
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getAgentPaymentEntryDetails(User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;

            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId());
            requestJson.put("userid", aUser.getUserId());


            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_Payment");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray detailsJsonList = aJsonObj.getJSONArray("details");
            ArrayList<PaymentTypeDetails> paymentDetailsList = new ArrayList<>();

            AgentPayment agentPayment = new AgentPayment();
            agentPayment.setUnreachedAmount(aJsonObj.getString("unreachedamount"));
            agentPayment.setTotalOutstandingAmount(aJsonObj.getString("totaloutstandamount"));
            for (int i =0; i<detailsJsonList.length();i++)
            {
                JSONObject paymentDetailsObject = detailsJsonList.getJSONObject(i);
                PaymentTypeDetails paymentTypeDetails = new PaymentTypeDetails();
                paymentTypeDetails.setPaymentDate(paymentDetailsObject.getString("date"));
                paymentTypeDetails.setPaymentAmount(paymentDetailsObject.getDouble("policyamt"));
                paymentTypeDetails.setServiceAmount(paymentDetailsObject.getDouble("serviceamt"));
                paymentDetailsList.add(paymentTypeDetails);

            }
            agentPayment.setDetailsList(paymentDetailsList);

            JSONArray roleDetailsJsonList = aJsonObj.getJSONArray("role");
            ArrayList<Role> paidToList = new ArrayList<>();


            for (int i =0; i<roleDetailsJsonList.length();i++)
            {
                JSONObject roleObject = roleDetailsJsonList.getJSONObject(i);
                Role aRole = new Role();
                aRole.setRoleId(roleObject.getString("roleid"));
                aRole.setRoleName(roleObject.getString("rolename"));

                JSONArray collectorJsonList = roleObject.getJSONArray("collname");
                ArrayList<String> collectorList = new ArrayList<>();
                ArrayList<String> collectorIdList = new ArrayList<>();
                for (int j =0; j<collectorJsonList.length();j++)
                {
                    JSONObject collectorObject = collectorJsonList.getJSONObject(j);
                    collectorList.add(collectorObject.getString("collname"));
                    collectorIdList.add(collectorObject.getString("roleid"));
                }
                aRole.setCollectorsId(collectorIdList);
                aRole.setCollectorsName(collectorList);
                paidToList.add(aRole);

            }
            agentPayment.setPaidUserList(paidToList);
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            Elixir.setAgentPayment(agentPayment);
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendAgentPaymentEntry(PaymentEntry aPaymentEntry, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("paidto", aPaymentEntry.getPaidTo());
            requestJson.put("collname", aPaymentEntry.getCollectorName());
            requestJson.put("chequeamt", aPaymentEntry.getChequeAmount()+"");
            requestJson.put("cashamt", aPaymentEntry.getCashAmount() + "");
            requestJson.put("totalamt", aPaymentEntry.getTotalAmount() + "");
            requestJson.put("paidamt", aPaymentEntry.getPaidAmount() + "");
            requestJson.put("diffamt", aPaymentEntry.getDifferenceAmount() + "");
            requestJson.put("remarks", aPaymentEntry.getRemarks());
            requestJson.put("password", aPaymentEntry.getCollectorPassword());
            JSONArray jsonArray = new JSONArray();
            for (PaymentTypeDetails aPaymentTypeDetails:aPaymentEntry.getPaymentTypeDetailsArrayList())
            {
                JSONObject aJsonObj = new JSONObject();
                aJsonObj.put("date",aPaymentTypeDetails.getPaymentDate());
                jsonArray.put(aJsonObj);
            }
            requestJson.put("policylist", jsonArray);


            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_payment1");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();

                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (Exception e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getPaymentReportDetails(User aUser, Context context, Handler handler, String date, String status)
    {
        try
        {
            this.context = context;

            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId());
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("date", date);
            requestJson.put("status", status.toLowerCase());


            URL url = new URL(API_ENDPOINT_PROD + "api/paymentreport");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray paymentJsonList = aJsonObj.getJSONArray("eList");
            ArrayList<Payment> paymentList = new ArrayList<>();

            for (int i =0; i<paymentJsonList.length();i++)
            {
                JSONObject paymentEntryObject = paymentJsonList.getJSONObject(i);

                    Payment aPayment = new Payment();
                    aPayment.setName(paymentEntryObject.getString("name"));
                    if(!paymentEntryObject.getString("cms").equalsIgnoreCase("null"))
                        aPayment.setCms(paymentEntryObject.getDouble("cms"));
                    else
                        aPayment.setCms(0);
                    if(!paymentEntryObject.getString("service").equalsIgnoreCase("null"))
                        aPayment.setService(paymentEntryObject.getDouble("service"));
                    else
                        aPayment.setService(0);
                    if(!paymentEntryObject.getString("otw").equalsIgnoreCase("null"))
                        aPayment.setOtw(paymentEntryObject.getDouble("otw"));
                    else
                        aPayment.setOtw(0);
                paymentList.add(aPayment);
            }
            JSONArray paymentReportJsonList = aJsonObj.getJSONArray("List");
            ArrayList<PaymentReport> paymentReportList = new ArrayList<>();

            for (int i =0; i<paymentReportJsonList.length();i++)
            {
                JSONObject paymentReportObject = paymentReportJsonList.getJSONObject(i);

                PaymentReport aPaymentReport = new PaymentReport();
                aPaymentReport.setName(paymentReportObject.getString("username"));
                aPaymentReport.setLocation(paymentReportObject.getString("location"));
                aPaymentReport.setAmountType(paymentReportObject.getString("amounttype"));
                aPaymentReport.setPaymentMode(paymentReportObject.getString("paymode"));
                if(!paymentReportObject.getString("totamt").equalsIgnoreCase("null"))
                    aPaymentReport.setTotalAmount(paymentReportObject.getString("totamt"));
                else
                    aPaymentReport.setTotalAmount("0");
                if(!paymentReportObject.getString("paidamt").equalsIgnoreCase("null"))
                    aPaymentReport.setPaidAmount(paymentReportObject.getString("paidamt"));
                else
                    aPaymentReport.setPaidAmount("0");
                if(!paymentReportObject.getString("differentamt").equalsIgnoreCase("null"))
                    aPaymentReport.setDiffernceAmount(paymentReportObject.getString("differentamt"));
                else
                    aPaymentReport.setDiffernceAmount("0");
                paymentReportList.add(aPaymentReport);


            }
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            Elixir.setPaymentList(paymentList);
            Elixir.setPaymentReportList(paymentReportList);
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendPaymentEntry(PaymentEntry aPaymentEntry, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("exeuserid", aPaymentEntry.getExecutiveUserId());
            requestJson.put("paymenttype", aPaymentEntry.getPaymentType());
            requestJson.put("chequeamt", aPaymentEntry.getChequeAmount()+"");
            requestJson.put("cashamt", aPaymentEntry.getCashAmount() + "");
            requestJson.put("totalamt", aPaymentEntry.getTotalAmount() + "");
            requestJson.put("paidamt", aPaymentEntry.getPaidAmount()+"");
            requestJson.put("diffamt", aPaymentEntry.getDifferenceAmount()+"");
            requestJson.put("remarks", aPaymentEntry.getRemarks());
            JSONArray jsonArray = new JSONArray();
            for (PaymentTypeDetails aPaymentTypeDetails:aPaymentEntry.getPaymentTypeDetailsArrayList())
            {
                JSONObject aJsonObj = new JSONObject();
                aJsonObj.put("date",aPaymentTypeDetails.getPaymentDate());
                jsonArray.put(aJsonObj);
            }
            requestJson.put(aPaymentEntry.getPaymentType()+"list", jsonArray);


            URL url = new URL(API_ENDPOINT_PROD + "api/paymententry");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();

                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (Exception e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", e.getMessage());
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    private String writeToSDFile(String pdfId, byte[] decodedPDF, boolean isPolicyPDF){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();

        File dir = new File (root.getAbsolutePath() + "/ElixirPDF");
        if(isPolicyPDF)
            dir = new File (root.getAbsolutePath() + "/ElixirPolicyPDF");
        dir.mkdirs();
        File file = new File(dir, pdfId +".pdf");

        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(decodedPDF);
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    private String writeToSDFile(ServiceReceipt aServiceReceipt, byte[] decodedPDF){

        String extr = Environment.getExternalStorageDirectory().toString();

        File dir = new File (extr + "/ElixirPDF/ServiceReceipt");
        dir.mkdirs();
        File file = new File(dir, aServiceReceipt.getName()+aServiceReceipt.getMobileNo() +".pdf");

        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(decodedPDF);
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();

        }
        return file.getPath();
    }

    public void getCMSList( User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("branchid", aUser.getBranchId());
            requestJson.put("locationid", aUser.getLocationId());
            requestJson.put("tlid", aUser.getTeamLeaderId());

            URL url = new URL(API_ENDPOINT_PROD + "api/Cms_new");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray eListArray = aJsonObj.getJSONArray("eList");
            JSONArray staffNameJsonArray = aJsonObj.getJSONArray("staffnamelist");
            JSONArray cashCollectorNameJsonArray = aJsonObj.getJSONArray("cashcollector");

            if(eListArray != null && eListArray.length() > 0)
            {
                if(theCMSListDataSource == null)
                    theCMSListDataSource = new CMSListDataSource(context);
                theCMSListDataSource.clearCMSList();

                JSONObject elistJsonObj = eListArray.getJSONObject(0);

                Message message = new Message();
                Bundle mBundle = new Bundle();
                saveCMSList(elistJsonObj);
                mBundle.putString("totalCMSAmount", elistJsonObj.getString("totalcms"));
                mBundle.putString("totalServiceAmount", elistJsonObj.getString("totalservice"));
                mBundle.putString("totalNonCMSAmount", elistJsonObj.getString("totalnoncms"));


                Elixir.setStaffNameArrayList(staffNameJsonArray);
                Elixir.setCashCollectorNameJsonArray(cashCollectorNameJsonArray);

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else if(staffNameJsonArray != null || cashCollectorNameJsonArray != null)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("totalCMSAmount", "0");
                mBundle.putString("totalServiceAmount", "0");
                mBundle.putString("totalNonCMSAmount", "0");


                Elixir.setStaffNameArrayList(staffNameJsonArray);
                Elixir.setCashCollectorNameJsonArray(cashCollectorNameJsonArray);

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    private void saveCMSList(JSONObject eListJsonObj)
    {
        try {
            JSONArray cmsJsonArray = eListJsonObj.getJSONArray("cmslist");
            JSONArray serviceJsonArray = eListJsonObj.getJSONArray("servicelist");
            JSONArray otwJsonArray = eListJsonObj.getJSONArray("noncmslist");
            for(int i=0; i<cmsJsonArray.length(); i++ ) {
                JSONObject aJsonObj = cmsJsonArray.getJSONObject(i);
                CMSList aCMSList = new CMSList();
                aCMSList.setCmsDateStr(aJsonObj.getString("cmsdate"));
                aCMSList.setCmsAmount(aJsonObj.getString("cmsamount"));
                aCMSList.setAmountType(CMSAmountType.CMS.getValue());
                theCMSListDataSource.createCMSList(aCMSList);
            }
            for(int i=0; i<serviceJsonArray.length(); i++ ) {
                JSONObject aJsonObj = serviceJsonArray.getJSONObject(i);
                CMSList aCMSList = new CMSList();
                aCMSList.setCmsDateStr(aJsonObj.getString("servicedatee"));
                aCMSList.setCmsAmount(aJsonObj.getString("serviceamount"));
                aCMSList.setAmountType(CMSAmountType.SERVICE.getValue());
                theCMSListDataSource.createCMSList(aCMSList);
            }
            for(int i=0; i<otwJsonArray.length(); i++ ) {
                JSONObject aJsonObj = otwJsonArray.getJSONObject(i);
                CMSList aCMSList = new CMSList();
                aCMSList.setCmsDateStr(aJsonObj.getString("noncmsdatee"));
                aCMSList.setCmsVehiNo(aJsonObj.getString("vehicleno"));
                aCMSList.setCmsVehiType(aJsonObj.getString("vehicletype"));
                aCMSList.setCmsAmount(aJsonObj.getString("noncmsamount"));
                aCMSList.setAmountType(CMSAmountType.NONCMS.getValue());
                theCMSListDataSource.createCMSList(aCMSList);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sendCMS(CMS aCMS, String amountType, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();

            requestJson.put("userid", aUser.getUserId());
            requestJson.put("totalamt", aCMS.getTotalCMSAmount()+"");
            requestJson.put("paidamt", aCMS.getPaidAmount()+"");
            requestJson.put("diffamt", (aCMS.getTotalCMSAmount() - aCMS.getPaidAmount())+"");
            requestJson.put("paidby", aCMS.getPaidBy());
            requestJson.put("transid", aCMS.getTransactionId());
            requestJson.put("paiddate", aCMS.getPaidDate());
            requestJson.put("paidaccno", aCMS.getPaidAccNo());
            requestJson.put("cashcollname", aCMS.getCashcollName());
            requestJson.put("staffname", aCMS.getStaffName());
            requestJson.put("paymode", aCMS.getPaymentMode());
            requestJson.put("remarks", aCMS.getRemarks());
            requestJson.put("otp", aCMS.getOtp());
            JSONArray jsonArray = new JSONArray();
            for (CMSList aCMSList:aCMS.getCmsLists())
            {
                JSONObject aJsonObj = new JSONObject();
                aJsonObj.put("date",aCMSList.getCmsDateStr());
                if(amountType == "Non-CMS")
                {
                    aJsonObj.put("vehicleno",aCMSList.getCmsVehiNo());
                }
                jsonArray.put(aJsonObj);
            }
            requestJson.put("cmslist", jsonArray);

            URL url = new URL(API_ENDPOINT_PROD + "api/payment1_executive");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendPolicy(Policy aPolicy, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId()+"");
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("roleid", aUser.getRoleId()+"");

            requestJson.put("name", aPolicy.getCustomerName());
            requestJson.put("address", aPolicy.getAddress());
            requestJson.put("mobile", aPolicy.getMobileNo());
            requestJson.put("companyname", aPolicy.getCompanyName());
            requestJson.put("vehicletype", aPolicy.getVehicleType());
            requestJson.put("vehicleno", aPolicy.getVehicleNo().toUpperCase());
            requestJson.put("engineno", aPolicy.getEngineNo().toUpperCase());
            requestJson.put("chasisno", aPolicy.getChasisNo().toUpperCase());
            requestJson.put("Dateofreg", aPolicy.getDateOfReg());
            requestJson.put("make", aPolicy.getMake());
            requestJson.put("model", aPolicy.getModel());
            requestJson.put("idv", aPolicy.getIdv()+"");
            requestJson.put("cc", aPolicy.getCc());
            requestJson.put("previousinsurance", aPolicy.isHavingPreviousInsurance()?"yes":"no");
            requestJson.put("ncb", aPolicy.getNcb());
            requestJson.put("previouscompany", aPolicy.getPreviousInsurance()+"");
            requestJson.put("od", aPolicy.getOdDiscount()+"");
            requestJson.put("premiumamt", aPolicy.getPreminumAmount()+"");
            requestJson.put("servicecharge", aPolicy.getServiceCharege()+"");
            requestJson.put("riskdate", aPolicy.getRiskStartDate());
            requestJson.put("finstatus", aPolicy.isHavingFinance()?"yes":"no");
            requestJson.put("finname", aPolicy.getFinancierName());

            requestJson.put("paymode", aPolicy.getPaymentType());
            requestJson.put("policytype", aPolicy.getPolicyType());
            requestJson.put("policyprepareby", aPolicy.getPolicyPreparedBy());

            requestJson.put("imt23", aPolicy.isImtStatus()?"yes":"no");
            requestJson.put("niltip", aPolicy.isNilDIPStatus()?"yes":"no");
            requestJson.put("year", aPolicy.getYearOfManufacture());
            requestJson.put("claims", aPolicy.isClaimsStatus()?"yes":"no");
            requestJson.put("nametransfer", aPolicy.isNameTransferStatus()?"yes":"no");
            requestJson.put("previousno", aPolicy.getPreviousInsuranceNo());
            requestJson.put("email", aPolicy.getEmailId());


            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_Addpolicy");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                mBundle.putString("policyId", (String)aJsonObj.get("policyid"));
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendPolicyImage(Policy aPolicy, User aUser, Context context, Handler handler, int id, String vehicleNo)
    {
        try
        {
            this.context = context;
            JSONObject requestFinalJson = new JSONObject();
            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_Policyimages");
            if(vehicleNo.length() == 0)
                requestFinalJson.put("policyid", aPolicy.getPolicyId());
            else {
                url = new URL(API_ENDPOINT_PROD + "api/Agency_Policyimages2");
                requestFinalJson.put("vehicleno", vehicleNo);
            }
            JSONObject requestJson = new JSONObject();
            String encodedImage = "";
            switch (id)
            {
                case R.id.btn_image1:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage1(), Base64.DEFAULT);
                    requestJson.put("imgname", "Right Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image2:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage2(), Base64.DEFAULT);
                    requestJson.put("imgname", "Left Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image3:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage3(), Base64.DEFAULT);
                    requestJson.put("imgname", "Front Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image4:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage4(), Base64.DEFAULT);
                    requestJson.put("imgname", "Back Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image5:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage5(), Base64.DEFAULT);

                    requestJson.put("imgname", "ODO Meter Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image6:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage6(), Base64.DEFAULT);
                    requestJson.put("imgname", "Engine No / Chasis No");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image7:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage7(), Base64.DEFAULT);
                    requestJson.put("imgname", "Engine Compartment");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image8:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage8(), Base64.DEFAULT);
                    requestJson.put("imgname", "Register NO");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image9:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage9(), Base64.DEFAULT);
                    requestJson.put("imgname", "Close Snap of Battery");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image10:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage10(), Base64.DEFAULT);
                    requestJson.put("imgname", "Under Carriage Photos");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image11:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage11(), Base64.DEFAULT);
                    requestJson.put("imgname", "Close Snap of Dashboard");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image12:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage12(), Base64.DEFAULT);
                    requestJson.put("imgname", "Wild Screen Glass");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image13:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getImage13(), Base64.DEFAULT);
                    requestJson.put("imgname", "Dickey Photos");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image1:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getDocImage1(), Base64.DEFAULT);
                    requestJson.put("imgname", "RC Copy");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image2:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getDocImage2(), Base64.DEFAULT);
                    requestJson.put("imgname", "Vehicle Invoice Copy");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image3:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getDocImage3(), Base64.DEFAULT);
                    requestJson.put("imgname", "Previous Policy Copy");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image4:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getDocImage4(), Base64.DEFAULT);
                    requestJson.put("imgname", "Non Intact Vehicle Parts");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image5:
                {
                    encodedImage = Base64.encodeToString(aPolicy.getDocImage5(), Base64.DEFAULT);
                    requestJson.put("imgname", "Cheque Photos");
                    requestJson.put("image", encodedImage);
                    break;
                }
            }
            ArrayList<JSONObject> imagesJsonArray = new ArrayList<JSONObject>();
            imagesJsonArray.add(requestJson);
            requestFinalJson.put("imagelist", new JSONArray(imagesJsonArray));
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestFinalJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendOTWImage(OTW aOTW, User aUser, Context context, Handler handler, int id, String vehicleNo)
    {
        try
        {
            this.context = context;
            JSONObject requestFinalJson = new JSONObject();
            URL url = new URL(API_ENDPOINT_PROD + "api/Otw_Policyimages");
            if(vehicleNo.length() == 0)
                requestFinalJson.put("policyid", aOTW.getOtwId());
            else {
                url = new URL(API_ENDPOINT_PROD + "api/Otw_Policyimages2");
                requestFinalJson.put("vehicleno", vehicleNo);
            }
            JSONObject requestJson = new JSONObject();
            String encodedImage = "";
            switch (id)
            {
                case R.id.btn_image1:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage1(), Base64.DEFAULT);
                    requestJson.put("imgname", "Right Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image2:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage2(), Base64.DEFAULT);
                    requestJson.put("imgname", "Left Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image3:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage3(), Base64.DEFAULT);
                    requestJson.put("imgname", "Front Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image4:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage4(), Base64.DEFAULT);
                    requestJson.put("imgname", "Back Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image5:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage5(), Base64.DEFAULT);

                    requestJson.put("imgname", "ODO Meter Side");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image6:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage6(), Base64.DEFAULT);
                    requestJson.put("imgname", "Engine No / Chasis No");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image7:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage7(), Base64.DEFAULT);
                    requestJson.put("imgname", "Engine Compartment");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image8:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage8(), Base64.DEFAULT);
                    requestJson.put("imgname", "Register NO");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image9:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage9(), Base64.DEFAULT);
                    requestJson.put("imgname", "Close Snap of Battery");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image10:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage10(), Base64.DEFAULT);
                    requestJson.put("imgname", "Under Carriage Photos");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image11:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage11(), Base64.DEFAULT);
                    requestJson.put("imgname", "Close Snap of Dashboard");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image12:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage12(), Base64.DEFAULT);
                    requestJson.put("imgname", "Wild Screen Glass");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_image13:
                {
                    encodedImage = Base64.encodeToString(aOTW.getImage13(), Base64.DEFAULT);
                    requestJson.put("imgname", "Dickey Photos");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image1:
                {
                    encodedImage = Base64.encodeToString(aOTW.getDocImage1(), Base64.DEFAULT);
                    requestJson.put("imgname", "RC Copy");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image2:
                {
                    encodedImage = Base64.encodeToString(aOTW.getDocImage2(), Base64.DEFAULT);
                    requestJson.put("imgname", "Vehicle Invoice Copy");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image3:
                {
                    encodedImage = Base64.encodeToString(aOTW.getDocImage3(), Base64.DEFAULT);
                    requestJson.put("imgname", "Previous Policy Copy");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image4:
                {
                    encodedImage = Base64.encodeToString(aOTW.getDocImage4(), Base64.DEFAULT);
                    requestJson.put("imgname", "Non Intact Vehicle Parts");
                    requestJson.put("image", encodedImage);
                    break;
                }
                case R.id.btn_doc_image5:
                {
                    encodedImage = Base64.encodeToString(aOTW.getDocImage5(), Base64.DEFAULT);
                    requestJson.put("imgname", "Cheque Photos");
                    requestJson.put("image", encodedImage);
                    break;
                }
            }
            ArrayList<JSONObject> imagesJsonArray = new ArrayList<JSONObject>();
            imagesJsonArray.add(requestJson);
            requestFinalJson.put("imagelist", new JSONArray(imagesJsonArray));
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestFinalJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendAllPolicyImage(Policy aPolicy, User aUser, Context context, Handler handler, String vehicleNo)
    {
        try
        {
            this.context = context;
            JSONObject requestFinalJson = new JSONObject();
            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_Policyimages");
            if(vehicleNo.length() == 0)
                requestFinalJson.put("policyid", aPolicy.getPolicyId());
            else {
                url = new URL(API_ENDPOINT_PROD + "api/Agency_Policyimages2");
                requestFinalJson.put("vehicleno", vehicleNo);
            }
            ArrayList<JSONObject> imagesJsonArray = new ArrayList<JSONObject>();

                if(aPolicy.getImage1() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage1(), Base64.DEFAULT);
                    requestJson.put("imgname", "Right Side");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage2() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage2(), Base64.DEFAULT);
                    requestJson.put("imgname", "Left Side");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage3() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage3(), Base64.DEFAULT);
                    requestJson.put("imgname", "Front Side");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage4() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage4(), Base64.DEFAULT);
                    requestJson.put("imgname", "Back Side");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage5() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage5(), Base64.DEFAULT);

                    requestJson.put("imgname", "ODO Meter Side");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage6() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage6(), Base64.DEFAULT);
                    requestJson.put("imgname", "Engine No / Chasis No");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage7() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage7(), Base64.DEFAULT);
                    requestJson.put("imgname", "Engine Compartment");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage8() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage8(), Base64.DEFAULT);
                    requestJson.put("imgname", "Register NO");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage9() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage9(), Base64.DEFAULT);
                    requestJson.put("imgname", "Close Snap of Battery");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage10() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage10(), Base64.DEFAULT);
                    requestJson.put("imgname", "Under Carriage Photos");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage11() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage11(), Base64.DEFAULT);
                    requestJson.put("imgname", "Close Snap of Dashboard");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage12() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage12(), Base64.DEFAULT);
                    requestJson.put("imgname", "Wild Screen Glass");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getImage13() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getImage13(), Base64.DEFAULT);
                    requestJson.put("imgname", "Dickey Photos");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getDocImage1() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getDocImage1(), Base64.DEFAULT);
                    requestJson.put("imgname", "RC Copy");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getDocImage2() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getDocImage2(), Base64.DEFAULT);
                    requestJson.put("imgname", "Vehicle Invoice Copy");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getDocImage3() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getDocImage3(), Base64.DEFAULT);
                    requestJson.put("imgname", "Previous Policy Copy");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getDocImage4() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getDocImage4(), Base64.DEFAULT);
                    requestJson.put("imgname", "Non Intact Vehicle Parts");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }
                if(aPolicy.getDocImage5() != null)
                {
                    JSONObject requestJson = new JSONObject();
                    String encodedImage = Base64.encodeToString(aPolicy.getDocImage5(), Base64.DEFAULT);
                    requestJson.put("imgname", "Cheque Photos");
                    requestJson.put("image", encodedImage);
                    imagesJsonArray.add(requestJson);
                }

            if(imagesJsonArray.size() == 0)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", "No Images to send");
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
                return;
            }

            requestFinalJson.put("imagelist", new JSONArray(imagesJsonArray));

            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestFinalJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendAllOTWImage(OTW aOTW, User aUser, Context context, Handler handler, String vehicleNo)
    {
        try
        {
            this.context = context;
            JSONObject requestFinalJson = new JSONObject();
            URL url = new URL(API_ENDPOINT_PROD + "api/Otw_Policyimages");
            if(vehicleNo.length() == 0)
                requestFinalJson.put("policyid", aOTW.getOtwId());
            else {
                url = new URL(API_ENDPOINT_PROD + "api/Otw_Policyimages2");
                requestFinalJson.put("vehicleno", vehicleNo);
            }
            ArrayList<JSONObject> imagesJsonArray = new ArrayList<JSONObject>();

            if(aOTW.getImage1() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage1(), Base64.DEFAULT);
                requestJson.put("imgname", "Right Side");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage2() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage2(), Base64.DEFAULT);
                requestJson.put("imgname", "Left Side");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage3() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage3(), Base64.DEFAULT);
                requestJson.put("imgname", "Front Side");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage4() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage4(), Base64.DEFAULT);
                requestJson.put("imgname", "Back Side");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage5() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage5(), Base64.DEFAULT);

                requestJson.put("imgname", "ODO Meter Side");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage6() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage6(), Base64.DEFAULT);
                requestJson.put("imgname", "Engine No / Chasis No");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage7() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage7(), Base64.DEFAULT);
                requestJson.put("imgname", "Engine Compartment");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage8() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage8(), Base64.DEFAULT);
                requestJson.put("imgname", "Register NO");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage9() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage9(), Base64.DEFAULT);
                requestJson.put("imgname", "Close Snap of Battery");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage10() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage10(), Base64.DEFAULT);
                requestJson.put("imgname", "Under Carriage Photos");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage11() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage11(), Base64.DEFAULT);
                requestJson.put("imgname", "Close Snap of Dashboard");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage12() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage12(), Base64.DEFAULT);
                requestJson.put("imgname", "Wild Screen Glass");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getImage13() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getImage13(), Base64.DEFAULT);
                requestJson.put("imgname", "Dickey Photos");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getDocImage1() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getDocImage1(), Base64.DEFAULT);
                requestJson.put("imgname", "RC Copy");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getDocImage2() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getDocImage2(), Base64.DEFAULT);
                requestJson.put("imgname", "Vehicle Invoice Copy");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getDocImage3() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getDocImage3(), Base64.DEFAULT);
                requestJson.put("imgname", "Previous Policy Copy");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getDocImage4() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getDocImage4(), Base64.DEFAULT);
                requestJson.put("imgname", "Non Intact Vehicle Parts");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }
            if(aOTW.getDocImage5() != null)
            {
                JSONObject requestJson = new JSONObject();
                String encodedImage = Base64.encodeToString(aOTW.getDocImage5(), Base64.DEFAULT);
                requestJson.put("imgname", "Cheque Photos");
                requestJson.put("image", encodedImage);
                imagesJsonArray.add(requestJson);
            }

            if(imagesJsonArray.size() == 0)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", "No Images to send");
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
                return;
            }

            requestFinalJson.put("imagelist", new JSONArray(imagesJsonArray));

            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestFinalJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getPolicyStatus(ArrayList<Policy> policyList, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            if(thePolicyDataSource == null)
                thePolicyDataSource = new PolicyDataSource(context);
            String policyIds = "";
            for(Iterator<Policy> i = policyList.iterator();i.hasNext();)
            {
                Policy aPolicy = i.next();
                if(aPolicy.getPolicyId() == null || aPolicy.getPolicyId().length() == 0)
                    continue;
                if(aPolicy.getStatus() != null && !aPolicy.getStatus().equalsIgnoreCase("Fresh"))
                    continue;;
                if (policyIds.length() == 0)
                    policyIds = aPolicy.getPolicyId();
                else
                    policyIds = policyIds + "," + aPolicy.getPolicyId();
            }
            JSONObject requestJson = new JSONObject();
            requestJson.put("policyids", policyIds);


            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_Policypdf");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray policyJsonList = aJsonObj.getJSONArray("eList");

            for (int i =0; i<policyJsonList.length();i++)
            {
                JSONObject policyObject = policyJsonList.getJSONObject(i);
                if(policyObject.getString("id").length()>0) {
                    Policy aPolicy = thePolicyDataSource.getPolicyById(policyObject.getString("id"));
                    aPolicy.setStatus(policyObject.getString("status"));

                    String pdfEncoded = policyObject.getString("pdf");
                    if (pdfEncoded.equalsIgnoreCase("0"))
                        aPolicy.setPdfPath("");
                    else {
                        byte[] decodedPdf = Base64.decode(pdfEncoded, Base64.DEFAULT);
                        aPolicy.setPdfPath(writeToSDFile(aPolicy.getPolicyId()+"_policy", decodedPdf, true));
                    }
                    thePolicyDataSource.updatePolicy(aPolicy);
                }

            }
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getAddPolicyDetails( User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId());
            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_PolicyDetails");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            parseAndSaveAddPolicyDetails(aJsonObj, false);
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);

        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getOTWDetails( User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId());
            URL url = new URL(API_ENDPOINT_PROD + "api/Otw_PolicyDetails");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            parseAndSaveAddPolicyDetails(aJsonObj, true);
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);

        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void checkVehicleNoIsValid( User aUser, Context context, Handler handler, String vehcileNo, boolean isOTW)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("vehicleno", vehcileNo);
            URL url = new URL(API_ENDPOINT_PROD + "api/Agency_Policyimages1");
            if(isOTW)
                url = new URL(API_ENDPOINT_PROD + "api/Otw_Policyimages1");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);

            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");

                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }

        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }


    private void parseAndSaveAddPolicyDetails(JSONObject aJsonObj, boolean isOTW)
    {
        try {
            if(!isOTW) {
                JSONArray makeJsonArray = aJsonObj.getJSONArray("eList");
                Elixir.setMakeJsonArray(makeJsonArray);
            }
            JSONArray companyJsonArray = new JSONArray();
            if(aJsonObj.has("Company"))
                companyJsonArray = aJsonObj.getJSONArray("Company");
            JSONArray vehicleJsonArray = new JSONArray();
            if(aJsonObj.has("vehicle"))
                vehicleJsonArray = aJsonObj.getJSONArray("vehicle");
            JSONArray insuranceJsonArray = new JSONArray();
            if(aJsonObj.has("preinsurance"))
                insuranceJsonArray = aJsonObj.getJSONArray("preinsurance");
            JSONArray ncbJsonArray = new JSONArray();
            if(aJsonObj.has("ncb"))
                ncbJsonArray = aJsonObj.getJSONArray("ncb");
            JSONArray periodJsonArray = new JSONArray();
            if(aJsonObj.has("period"))
                periodJsonArray = aJsonObj.getJSONArray("period");

            Elixir.setCompanyArrayList(companyJsonArray);
            Elixir.setVehicleArrayList(vehicleJsonArray);
            Elixir.setPreviousInsuranceArrayList(insuranceJsonArray);
            Elixir.setNcbArrayList(ncbJsonArray);
            Elixir.setPeriodJsonArray(periodJsonArray);
        }
        catch (JSONException e)
        {
        }

    }

    private void writePhotoToSDFile( byte[] decodedPDF){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

//        File root = android.os.Environment.getExternalStorageDirectory();

        String extr = Environment.getExternalStorageDirectory().toString();

        File dir = new File (extr + "/ElixirIMge/");
        dir.mkdirs();
        File file = new File(dir, "image" +".png");

        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(decodedPDF);
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();


        }
        String  str = file.getPath();

    }

    public void sendOTP(String otpUserID, String paidAmount, User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();

            requestJson.put("userid", aUser.getUserId());
            requestJson.put("senderid", otpUserID);
            requestJson.put("paidamt", paidAmount);

            URL url = new URL(API_ENDPOINT_PROD + "api/payment1_otp");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void getFilesList( User aUser, Context context, Handler handler, int count)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("fcount", ""+count);
            URL url = new URL(API_ENDPOINT_PROD + "api/Receivefiles_Executive");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                parseAndSaveFileDetails(aJsonObj);
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", "");
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }

        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    private void parseAndSaveFileDetails(JSONObject aJsonObj)
    {
        try {

            JSONArray fileList = aJsonObj.getJSONArray("filelist");
            ArrayList<com.invicibledevs.elixir.model.ReceiveFile> fileArrayList= new ArrayList<ReceiveFile>();
            for (int i=0;i<fileList.length();i++)
            {
                JSONObject fileJsonObj = fileList.getJSONObject(i);
                com.invicibledevs.elixir.model.ReceiveFile aFile = new com.invicibledevs.elixir.model.ReceiveFile();
                aFile.setSenderName(fileJsonObj.getString("sendername"));
                aFile.setMessage(fileJsonObj.getString("message"));
                aFile.setDate(fileJsonObj.getString("datee"));
                aFile.setFileName(fileJsonObj.getString("filename"));
                fileArrayList.add(aFile);
            }

            Elixir.setReceiveFilesList(fileArrayList);
        }
        catch (JSONException e) {
        }

    }

    public void downloadFile( User aUser, com.invicibledevs.elixir.model.ReceiveFile aFile, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", aUser.getUserId());
            requestJson.put("filename", aFile.getFileName());
            URL url = new URL(API_ENDPOINT_PROD + "api/Receivefiles_Executive1");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {

                Message message = new Message();
                Bundle mBundle = new Bundle();
                String pdfEncoded = aJsonObj.getString("File");
                if (!pdfEncoded.equalsIgnoreCase("0"))
                {
                    byte[] decodedPdf = Base64.decode(pdfEncoded, Base64.DEFAULT);
                    String res = writeReceivedFileToSDFile(aFile, decodedPdf);
                    mBundle.putString("logError", res);
                }
                mBundle.putString("responseStatus", "");
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
            else
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Error");
                message.setData(mBundle);
                message.what = 0;
                handler.sendMessage(message);
            }

        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    private String writeReceivedFileToSDFile(ReceiveFile aFile, byte[] decodedPDF){

        String extr = Environment.getExternalStorageDirectory().toString();

        File dir = new File (extr + "/ElixirFiles");
        dir.mkdirs();
        File file = new File(dir, aFile.getFileName());

        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(decodedPDF);
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "ErrorMSG" + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "ErrorMSG" + e.getMessage();

        }
        return file.getPath();
    }

    public void getServiceReceiptDetails( User aUser, Context context, Handler handler)
    {
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("branchid", aUser.getBranchId());
            URL url = new URL(API_ENDPOINT_PROD + "api/Service_PolicyDetails");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            parseAndSaveAddPolicyDetails(aJsonObj, true);//For Service receipt also we have to send as true
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);

        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void sendExecutiveReport(String fdate,String tdate,String uid,Context context, Handler handler)
    {
        try {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("fromdate", fdate);
            requestJson.put("todate", tdate);
            requestJson.put("userid", uid);

            URL url = new URL(API_ENDPOINT_PROD + "api/Overallreport");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            JSONArray executiveJsonList = aJsonObj.getJSONArray("exerep");
            ArrayList<ExecutiveReport> executiveReportList = new ArrayList<>();

            for (int i =0; i<executiveJsonList.length();i++)
            {
                JSONObject executiveEntryObject = executiveJsonList.getJSONObject(i);

                ExecutiveReport aExecutive = new ExecutiveReport();
                aExecutive.setdate(executiveEntryObject.getString("date"));
                if(!executiveEntryObject.getString("intime").equalsIgnoreCase("null"))
                    aExecutive.setIntime(executiveEntryObject.getString("intime"));
                else
                    aExecutive.setIntime("0");
                if(!executiveEntryObject.getString("outtime").equalsIgnoreCase("null"))
                    aExecutive.setOuttime(executiveEntryObject.getString("outtime"));
                else
                    aExecutive.setOuttime("0");
                if(!executiveEntryObject.getString("policycount").equalsIgnoreCase("null"))
                    aExecutive.setPolicycount(executiveEntryObject.getString("policycount"));
                else
                    aExecutive.setPolicycount("0");
                if(!executiveEntryObject.getString("leadscount").equalsIgnoreCase("null"))
                    aExecutive.setLeadscount(executiveEntryObject.getString("leadscount"));
                else
                    aExecutive.setLeadscount("0");
                if(!executiveEntryObject.getString("otwcount").equalsIgnoreCase("null"))
                    aExecutive.setOtwcount(executiveEntryObject.getString("otwcount"));
                else
                    aExecutive.setOtwcount("0");
                executiveReportList.add(aExecutive);
            }

            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "");
            mBundle.putString("msgTitle", "");
            Elixir.setExecutiveReportList(executiveReportList);
            message.setData(mBundle);
            message.what = 1;
            handler.sendMessage(message);
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }

    public void feedback(String message2,String value,String uid,Context context, Handler handler){
        try
        {
            this.context = context;
            JSONObject requestJson = new JSONObject();
            requestJson.put("userid", uid);
            requestJson.put("toid", value);
            requestJson.put("message", message2);

            URL url = new URL(API_ENDPOINT_PROD + "api/Feedback");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(true);
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(requestJson.toString());
            ps.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String responseString = reader.readLine();

            JSONObject aJsonObj = new JSONObject(responseString);
            boolean isSaved = Boolean.valueOf( (String)aJsonObj.get("isSaved"));

            if(isSaved)
            {
                Message message = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("responseStatus", (String)aJsonObj.get("message"));
                mBundle.putString("msgTitle", "Message");
                message.setData(mBundle);
                message.what = 1;
                handler.sendMessage(message);
            }
        }
        catch (IOException e) {
            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
        catch (JSONException e) {

            Message message = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putString("responseStatus", "The service could not be reached. Please try again later.");
            mBundle.putString("msgTitle", "Cannot Connect");
            message.setData(mBundle);
            message.what = 0;
            handler.sendMessage(message);
        }
    }
}
