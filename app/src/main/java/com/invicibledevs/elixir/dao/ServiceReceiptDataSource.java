package com.invicibledevs.elixir.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.invicibledevs.elixir.model.ServiceReceipt;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 18/11/15.
 */
public class ServiceReceiptDataSource {

    // ServiceReceipt table name
    public static final String TABLE_SERVICE_RECEIPT = "service_receipt";
    // ServiceReceipt Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    private static final String KEY_MOBILE_NO = "mobile_no";
    private static final String KEY_IMAGE1 = "image1";
    private static final String KEY_IMAGE2 = "image2";
    private static final String KEY_IMAGE3 = "image3";
    private static final String KEY_VEHICLE_NO = "vehicle_no";
    private static final String KEY_SERVICE_CHARGE = "service_charge";
    private static final String KEY_VEHICLE_TYPE = "vehicle_type";
    private static final String KEY_POLICY_PERIOD = "policy_period";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_POLICY_NO = "policy_no";
    private static final String KEY_POLICY_AMOUNT = "policy_amount";
    private static final String KEY_SIGNATURE_IMAGE = "signature_image";
    private static final String KEY_RECEIPT_DATE = "receipt_date";
    private static final String KEY_OD = "od";
    private static final String KEY_COMPANY_NAME = "company_name";

    public static String CREATE_SERVICE_RECEIPT_TABLE_QUERY = "CREATE TABLE " + TABLE_SERVICE_RECEIPT + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_MOBILE_NO + " TEXT,"
            + KEY_VEHICLE_NO + " TEXT," + KEY_SERVICE_CHARGE + " DOUBLE," + KEY_VEHICLE_TYPE + " TEXT,"
            + KEY_POLICY_PERIOD + " TEXT," + KEY_EMAIL + " TEXT," + KEY_POLICY_NO + " TEXT," + KEY_POLICY_AMOUNT + " DOUBLE," + KEY_IMAGE1 +" TEXT," + KEY_IMAGE2 +" TEXT," + KEY_IMAGE3 +" TEXT," + KEY_SIGNATURE_IMAGE +" TEXT," + KEY_RECEIPT_DATE+" TEXT," + KEY_OD + " DOUBLE," + KEY_COMPANY_NAME+" TEXT)";

    public static String SERVICE_RECEIPT_ID_INDEX_QUERY = "CREATE INDEX IF NOT EXISTS service_receipt_id_index ON " + TABLE_SERVICE_RECEIPT + "(" + KEY_ID + ")";
    private String deleteQuery = "DELETE FROM " + TABLE_SERVICE_RECEIPT;
    public static String SERVICE_RECEIPT_ALTER_QUERY1 = "ALTER TABLE " + TABLE_SERVICE_RECEIPT + " ADD " + KEY_SIGNATURE_IMAGE + " TEXT";
    public static String SERVICE_RECEIPT_ALTER_QUERY2 = "ALTER TABLE " + TABLE_SERVICE_RECEIPT + " ADD " + KEY_RECEIPT_DATE + " TEXT";
    public static String SERVICE_RECEIPT_ALTER_QUERY3 = "ALTER TABLE " + TABLE_SERVICE_RECEIPT + " ADD " + KEY_OD + " DOUBLE";
    public static String SERVICE_RECEIPT_ALTER_QUERY4 = "ALTER TABLE " + TABLE_SERVICE_RECEIPT + " ADD " + KEY_COMPANY_NAME + " TEXT";
    // Database fields
    private SQLiteDatabase database;
    private DatabaseHandler dbHelper;

    public ServiceReceiptDataSource(Context context) {
        dbHelper = DatabaseHandler.getInstance(context);
    }

    private void open() throws SQLException {
        database = dbHelper.getMyWritableDatabase();
    }

    public ServiceReceipt createServiceReceipt(ServiceReceipt theServiceReceipt)
    {
        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, theServiceReceipt.getName());
        values.put(KEY_MOBILE_NO, theServiceReceipt.getMobileNo());
        values.put(KEY_VEHICLE_NO, theServiceReceipt.getVehicleNo());
        values.put(KEY_SERVICE_CHARGE, theServiceReceipt.getServiceCharge());
        values.put(KEY_VEHICLE_TYPE, theServiceReceipt.getVehicleType());
        values.put(KEY_POLICY_PERIOD, theServiceReceipt.getPolicyPeriod());
        values.put(KEY_EMAIL, theServiceReceipt.getEmail());
        values.put(KEY_POLICY_NO, theServiceReceipt.getPolicyNo());
        values.put(KEY_POLICY_AMOUNT, theServiceReceipt.getPolicyAmount());
        values.put(KEY_IMAGE1, theServiceReceipt.getImage1());
        values.put(KEY_IMAGE2, theServiceReceipt.getImage2());
        values.put(KEY_IMAGE3, theServiceReceipt.getImage3());
        values.put(KEY_SIGNATURE_IMAGE, theServiceReceipt.getSignatureImage());
        values.put(KEY_RECEIPT_DATE, theServiceReceipt.getReceiptDate());
        values.put(KEY_OD, theServiceReceipt.getOd());
        values.put(KEY_COMPANY_NAME, theServiceReceipt.getCompanyName());
        // Inserting Row
        long rowId = database.insert(TABLE_SERVICE_RECEIPT, null, values);
        theServiceReceipt.setId((int) rowId);
        return theServiceReceipt;
    }

    public void deleteServiceReceiptById(String serviceReceiptId)
    {
        this.open();
        String selectQuery = "DELETE FROM " + TABLE_SERVICE_RECEIPT +" where " + KEY_ID + " = " + serviceReceiptId;
        database.execSQL(selectQuery);
    }

    /**
     * Clears the table
     */
    public void clearServiceReceipt()
    {
        this.open();
        database.execSQL(deleteQuery);
    }



    public ArrayList<ServiceReceipt> getAllServiceReceipts()
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_SERVICE_RECEIPT ;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<ServiceReceipt> serviceReceipts = new ArrayList<ServiceReceipt>();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return serviceReceipts;
        }
        do
        {
            ServiceReceipt aServiceReceipt = this.cursorToServiceReceipt(cursor);
            serviceReceipts.add(aServiceReceipt);
        }
        while(cursor.moveToNext());
        cursor.close();
        return serviceReceipts;
    }

    private ServiceReceipt cursorToServiceReceipt(Cursor cursor)
    {
        try
        {
            ServiceReceipt aServiceReceipt = new ServiceReceipt();
            aServiceReceipt.setId(cursor.getInt(0));
            aServiceReceipt.setName(cursor.getString(1));
            aServiceReceipt.setMobileNo(cursor.getString(2));
            aServiceReceipt.setVehicleNo(cursor.getString(3));
            aServiceReceipt.setServiceCharge(cursor.getDouble(4));
            aServiceReceipt.setVehicleType(cursor.getString(5));
            aServiceReceipt.setPolicyPeriod(cursor.getString(6));
            aServiceReceipt.setEmail(cursor.getString(7));
            aServiceReceipt.setPolicyNo(cursor.getString(8));
            aServiceReceipt.setPolicyAmount(cursor.getDouble(9));
            aServiceReceipt.setImage1(cursor.getBlob(10));
            aServiceReceipt.setImage2(cursor.getBlob(11));
            aServiceReceipt.setImage3(cursor.getBlob(12));
            aServiceReceipt.setSignatureImage(cursor.getBlob(13));
            aServiceReceipt.setReceiptDate(cursor.getString(14));
            aServiceReceipt.setOd(cursor.getDouble(15));
            aServiceReceipt.setCompanyName(cursor.getString(16));
            return aServiceReceipt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
