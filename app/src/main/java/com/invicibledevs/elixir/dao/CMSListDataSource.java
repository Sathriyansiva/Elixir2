package com.invicibledevs.elixir.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.invicibledevs.elixir.model.CMSList;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 23/12/15.
 */
public class CMSListDataSource {
    // OTW table name
    public static final String TABLE_CMS_LIST = "cms_list";
    // ServiceReceipt Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_DATE = "date";
    private static final String KEY_CMS_AMOUNT = "cms_amount";
    private static final String KEY_VEHICLE_NO = "vehicleno";
    private static final String KEY_VEHICLE_TYPE = "vehicletype";
    private static final String KEY_STATUS = "status";
    private static final String KEY_AMOUNT_TYPE = "amount_type";


    public static String CREATE_CMS_LIST_TABLE_QUERY = "CREATE TABLE " + TABLE_CMS_LIST + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT," + KEY_CMS_AMOUNT + " TEXT,"
            + KEY_VEHICLE_NO + " TEXT," + KEY_VEHICLE_TYPE + " TEXT," + KEY_STATUS + " TEXT," + KEY_AMOUNT_TYPE + " INTEGER)";


    public static String CMS_LIST_ID_INDEX_QUERY = "CREATE INDEX IF NOT EXISTS cms_list_id_index ON " + TABLE_CMS_LIST + "(" + KEY_ID + ")";
    private String deleteQuery = "DELETE FROM " + TABLE_CMS_LIST;
    public static String CMS_LIST_ALTER_QUERY1 = "ALTER TABLE " + TABLE_CMS_LIST + " ADD " + KEY_AMOUNT_TYPE + " INTEGER DEFAULT 0";
    // Database fields
    private SQLiteDatabase database;
    private DatabaseHandler dbHelper;


    public CMSListDataSource(Context context) {
        dbHelper = DatabaseHandler.getInstance(context);
    }

    private void open() throws SQLException {
        database = dbHelper.getMyWritableDatabase();
    }

    public void createCMSList(CMSList theCMSList)
    {
        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, theCMSList.getCmsDateStr());
        values.put(KEY_CMS_AMOUNT, theCMSList.getCmsAmount());
        values.put(KEY_VEHICLE_NO, theCMSList.getCmsVehiNo());
        values.put(KEY_VEHICLE_TYPE, theCMSList.getCmsVehiType());
        values.put(KEY_STATUS, theCMSList.getStatus());
        values.put(KEY_AMOUNT_TYPE, theCMSList.getAmountType());
        // Inserting Row
        long rowId = database.insert(TABLE_CMS_LIST, null, values);
        theCMSList.setId((int) rowId);
    }

    /**
     * Clears the table
     */
    public void clearCMSList()
    {
        this.open();
        database.execSQL(deleteQuery);
    }



    public ArrayList<CMSList> getCMSListByAmountType(int amountType)
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_CMS_LIST + " WHERE " + KEY_AMOUNT_TYPE + " = " +amountType;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<CMSList> cmsLists = new ArrayList<CMSList>();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return cmsLists;
        }
        do
        {
            CMSList aCMSList = this.cursorToCMSList(cursor);
            cmsLists.add(aCMSList);
        }
        while(cursor.moveToNext());
        cursor.close();
        return cmsLists;
    }


    private CMSList cursorToCMSList(Cursor cursor)
    {
        try
        {
            CMSList aCMSList = new CMSList();
            aCMSList.setId(cursor.getInt(0));
            aCMSList.setCmsDateStr(cursor.getString(1));
            aCMSList.setCmsAmount(cursor.getString(2));
            aCMSList.setCmsVehiNo(cursor.getString(3));
            aCMSList.setCmsVehiType(cursor.getString(4));
            aCMSList.setStatus(cursor.getString(5));
            aCMSList.setAmountType(cursor.getInt(6));
            return aCMSList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
