package com.invicibledevs.elixir.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This is eMeals Database handler
 * @author nandhakumar
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 9;
	// Database Name
	private static final String DATABASE_NAME = "ELIXIR";
	private static String[] CREATE_TABLE_ARRAY = {UserDataSource.CREATE_USER_TABLE_QUERY, AttendanceDataSource.CREATE_ATTENDANCE_TABLE_QUERY, ServiceReceiptDataSource.CREATE_SERVICE_RECEIPT_TABLE_QUERY, OTWDataSource.CREATE_OTW_NEW_TABLE_QUERY, CMSListDataSource.CREATE_CMS_LIST_TABLE_QUERY, PolicyDataSource.CREATE_POLICY_TABLE_QUERY};
	
	private static String[] INDEX_QUERY_ARRAY = {UserDataSource.USER_ID_INDEX_QUERY, AttendanceDataSource.ATTENDANCE_ID_INDEX_QUERY, ServiceReceiptDataSource.SERVICE_RECEIPT_ID_INDEX_QUERY, OTWDataSource.OTW_ID_INDEX_QUERY, CMSListDataSource.CMS_LIST_ID_INDEX_QUERY, PolicyDataSource.POLICY_ID_INDEX_QUERY};
	
	private static String[] ALTER_QUERY_ARRAY = {OTWDataSource.CREATE_OTW_NEW_TABLE_QUERY};
	private static String[] TABLE_ARRAY = {UserDataSource.TABLE_USER, AttendanceDataSource.TABLE_ATTENDANCE, ServiceReceiptDataSource.TABLE_SERVICE_RECEIPT, OTWDataSource.TABLE_OTW_NEW, CMSListDataSource.TABLE_CMS_LIST, PolicyDataSource.TABLE_POLICY};
	private static DatabaseHandler dbHelper = null;
	public static synchronized DatabaseHandler getInstance(Context theContext)
	{
		if(dbHelper == null)
			dbHelper = new DatabaseHandler(theContext);
		return dbHelper;
	}

	private SQLiteDatabase myWritableDb;
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		for (int i = 0; i < CREATE_TABLE_ARRAY.length; i++) {
			db.execSQL(CREATE_TABLE_ARRAY[i]);
		}
		for (int i = 0; i < INDEX_QUERY_ARRAY.length; i++) {
			db.execSQL(INDEX_QUERY_ARRAY[i]);
		}
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("**OLD VERSION:"+oldVersion);
		//creating index on db upgrade which will help to create indexes during over installation with previous db version.
		if(oldVersion < 2)//Run the following query if the db version is lesser than 2.
		{
			for (int i = 0; i < ALTER_QUERY_ARRAY.length; i++) {
				db.execSQL(ALTER_QUERY_ARRAY[i]);
			}

		}
		if(oldVersion<3)
		{
			db.execSQL(CMSListDataSource.CREATE_CMS_LIST_TABLE_QUERY);
		}
		if(oldVersion<4)
		{
			db.execSQL(ServiceReceiptDataSource.SERVICE_RECEIPT_ALTER_QUERY1);
			db.execSQL(ServiceReceiptDataSource.SERVICE_RECEIPT_ALTER_QUERY2);
		}
		if(oldVersion<5)
		{
			db.execSQL(UserDataSource.USER_ALTER_QUERY1);
			db.execSQL(UserDataSource.USER_ALTER_QUERY2);
			db.execSQL(PolicyDataSource.CREATE_POLICY_TABLE_QUERY);
		}
		if(oldVersion<6)
		{
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY1);
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY2);
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY3);
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY4);
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY5);
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY6);
			db.execSQL(PolicyDataSource.POLICY_ALTER_QUERY7);
		}
		if(oldVersion<7)
		{
			db.execSQL(OTWDataSource.CREATE_OTW_NEW_TABLE_QUERY);
		}
		if(oldVersion<8)
		{
			db.execSQL(CMSListDataSource.CMS_LIST_ALTER_QUERY1);
			db.execSQL(ServiceReceiptDataSource.SERVICE_RECEIPT_ALTER_QUERY3);
		}
		if(oldVersion<9)
		{
			db.execSQL(ServiceReceiptDataSource.SERVICE_RECEIPT_ALTER_QUERY4);
			db.execSQL(OTWDataSource.OTW_ALTER_QUERY1);
			db.execSQL(OTWDataSource.OTW_ALTER_QUERY2);
		}
		for (int i = 0; i < INDEX_QUERY_ARRAY.length; i++) {
			db.execSQL(INDEX_QUERY_ARRAY[i]);
		}
	}
	
	public void clearAllTables(DatabaseHandler dbHelper)
	{
		SQLiteDatabase database = dbHelper.getMyWritableDatabase();
		for (int i = 0; i < TABLE_ARRAY.length; i++) {
			database.execSQL("DELETE FROM " + TABLE_ARRAY[i]);
		}
	}
	
	/**
     * Returns a writable database instance in order not to open and close many
     * SQLiteDatabase objects simultaneously
     *
     * @return a writable instance to SQLiteDatabase
     */
    public synchronized SQLiteDatabase getMyWritableDatabase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }
        return myWritableDb;
    }
 
    public synchronized void closeMyWritableDatabase() {
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }
}
