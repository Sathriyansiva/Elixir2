package com.invicibledevs.elixir.helper;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.invicibledevs.elixir.dao.DatabaseHandler;
import com.invicibledevs.elixir.model.AgentPayment;
import com.invicibledevs.elixir.model.ExecutiveReport;
import com.invicibledevs.elixir.model.Payment;
import com.invicibledevs.elixir.model.PaymentReport;
import com.invicibledevs.elixir.model.ReceiveFile;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nandhakumarv on 14/11/15.
 */
public class Elixir extends Application {

    private DatabaseHandler dbHelper;

    public static ArrayList<Payment> getPaymentList() {
        return paymentList;
    }

    public static void setPaymentList(ArrayList<Payment> paymentList) {
        Elixir.paymentList = paymentList;
    }

    private static ArrayList<Payment> paymentList;

    public static ArrayList<PaymentReport> getPaymentReportList() {
        return paymentReportList;
    }

    public static void setPaymentReportList(ArrayList<PaymentReport> paymentReportList) {
        Elixir.paymentReportList = paymentReportList;
    }

    private static ArrayList<PaymentReport> paymentReportList;

    private static AgentPayment agentPayment;

    public static AgentPayment getAgentPayment() {
        return agentPayment;
    }

    public static void setAgentPayment(AgentPayment agentPayment) {
        Elixir.agentPayment = agentPayment;
    }


    private static JSONArray companyArrayList;
    private static JSONArray vehicleArrayList;
    private static JSONArray previousInsuranceArrayList;
    private static JSONArray ncbArrayList;
    private static JSONArray makeJsonArray;
    private static JSONArray staffNameArrayList;
    private static JSONArray cashCollectorNameJsonArray;
    private static JSONArray periodJsonArray;
    private static ArrayList<ReceiveFile> receiveFilesList;
    private static ArrayList<ExecutiveReport> executiveReportList;

    public static JSONArray getCompanyArrayList() {
        return companyArrayList;
    }

    public static void setCompanyArrayList(JSONArray companyArrayList) {
        Elixir.companyArrayList = companyArrayList;
    }

    public static JSONArray getVehicleArrayList() {
        return vehicleArrayList;
    }

    public static void setVehicleArrayList(JSONArray vehicleArrayList) {
        Elixir.vehicleArrayList = vehicleArrayList;
    }

    public static JSONArray getPreviousInsuranceArrayList() {
        return previousInsuranceArrayList;
    }

    public static void setPreviousInsuranceArrayList(JSONArray previousInsuranceArrayList) {
        Elixir.previousInsuranceArrayList = previousInsuranceArrayList;
    }

    public static JSONArray getNcbArrayList() {
        return ncbArrayList;
    }

    public static void setNcbArrayList(JSONArray ncbArrayList) {
        Elixir.ncbArrayList = ncbArrayList;
    }

    public static JSONArray getMakeJsonArray() {
        return makeJsonArray;
    }

    public static void setMakeJsonArray(JSONArray makeJsonArray) {
        Elixir.makeJsonArray = makeJsonArray;
    }

    public static JSONArray getStaffNameArrayList() {
        return staffNameArrayList;
    }

    public static void setStaffNameArrayList(JSONArray staffNameArrayList) {
        Elixir.staffNameArrayList = staffNameArrayList;
    }

    public static JSONArray getCashCollectorNameJsonArray() {
        return cashCollectorNameJsonArray;
    }

    public static void setCashCollectorNameJsonArray(JSONArray cashCollectorNameJsonArray) {
        Elixir.cashCollectorNameJsonArray = cashCollectorNameJsonArray;
    }

    public DatabaseHandler getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(DatabaseHandler dbHelper) {
        this.dbHelper = dbHelper;
    }


    public static ArrayList<ReceiveFile> getReceiveFilesList() {
        return receiveFilesList;
    }

    public static void setReceiveFilesList(ArrayList<ReceiveFile> receiveFilesList) {
        if(Elixir.receiveFilesList == null)
            Elixir.receiveFilesList = receiveFilesList;
        else
            Elixir.receiveFilesList.addAll(receiveFilesList);
    }

    public static void setUpdatedReceiveFilesList(ArrayList<ReceiveFile> receiveFilesList) {
        Elixir.receiveFilesList = receiveFilesList;
    }

    public static JSONArray getPeriodJsonArray() {
        return periodJsonArray;
    }

    public static void setPeriodJsonArray(JSONArray periodJsonArray) {
        Elixir.periodJsonArray = periodJsonArray;
    }

    public static ArrayList<ExecutiveReport> getExecutiveReportList() {
        return executiveReportList;
    }

    public static void setExecutiveReportList(ArrayList<ExecutiveReport> executiveReportList) {
        Elixir.executiveReportList = executiveReportList;
    }

    @Override
    public void onCreate() {
        if(dbHelper == null)
            dbHelper = DatabaseHandler.getInstance(getApplicationContext());
        dbHelper.getMyWritableDatabase();
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        if(dbHelper != null)
            dbHelper.closeMyWritableDatabase();
        super.onTerminate();
    }

    public static Bitmap imageOreintationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return bitmap;
    }
}
