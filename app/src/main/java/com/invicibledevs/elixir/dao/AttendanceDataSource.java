package com.invicibledevs.elixir.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.invicibledevs.elixir.model.Attendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AttendanceDataSource
{
		// Attendance table name
		public static final String TABLE_ATTENDANCE = "attendance";
		// Attendance Table Columns names
		public static final String KEY_ID = "id";
		public static final String KEY_LOCK_DATE = "lock_date";
		private static final String KEY_BALANCE_AMOUNT = "balance_amount";
		private static final String KEY_IMAGE = "image";
		private static final String KEY_TW_COUNT = "tw_count";
		private static final String KEY_TW_CMS_AMOUNT = "tw_cms_amount";
		private static final String KEY_TW_SERVICE_CHARGE = "tw_service_charge";
		public static final String KEY_IS_IN_TIME = "is_in_time";
		
		public static String CREATE_ATTENDANCE_TABLE_QUERY = "CREATE TABLE " + TABLE_ATTENDANCE + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_LOCK_DATE + " TEXT,"
				+ KEY_BALANCE_AMOUNT + " DOUBLE," + KEY_IMAGE + " TEXT," + KEY_TW_COUNT + " INTEGER,"
				+ KEY_TW_CMS_AMOUNT + " DOUBLE," + KEY_TW_SERVICE_CHARGE + " DOUBLE," + KEY_IS_IN_TIME + " INTEGER)";

		public static String ATTENDANCE_ID_INDEX_QUERY = "CREATE INDEX IF NOT EXISTS attendance_id_index ON " + TABLE_ATTENDANCE + "(" + KEY_ID + ")";
		private String deleteQuery = "DELETE FROM " + TABLE_ATTENDANCE;
		
		  // Database fields
		  private SQLiteDatabase database;
		  private DatabaseHandler dbHelper;

		  public AttendanceDataSource(Context context) {
			  dbHelper = DatabaseHandler.getInstance(context);
		  }

		  private void open() throws SQLException {
		    database = dbHelper.getMyWritableDatabase();
		  }

		  public void createAttendance(Attendance theAttendance)
		  {
			  	this.open();
				ContentValues values = new ContentValues();
			  	if(theAttendance.getLockDate() != null)
				  	values.put(KEY_LOCK_DATE, theAttendance.getLockDate().toString());
				else
				  	values.put(KEY_LOCK_DATE, "");
				values.put(KEY_BALANCE_AMOUNT, theAttendance.getBalanceAmount());
				values.put(KEY_IMAGE, theAttendance.getImage());
				values.put(KEY_TW_COUNT, theAttendance.getTwCount());
				values.put(KEY_TW_CMS_AMOUNT, theAttendance.getTwCMSAmount());
				values.put(KEY_TW_SERVICE_CHARGE, theAttendance.getTwServiceCharge());
				values.put(KEY_IS_IN_TIME, theAttendance.isInTime());
				// Inserting Row
				long rowId = database.insert(TABLE_ATTENDANCE, null, values);
				theAttendance.setId((int) rowId);
		  }

		  /**
		   * Clears the table
		   */
		  public void clearAttendance()
		  {
			  this.open();
			  database.execSQL(deleteQuery);
		  }
		  
		  
		  
		  public ArrayList<Attendance> getAllAttendance()
		  {
			  this.open();
			  String selectByUserNameQuery = "SELECT  * FROM " + TABLE_ATTENDANCE ;
			  Cursor cursor = database.rawQuery(selectByUserNameQuery, null);
			  cursor.moveToFirst();
			  ArrayList<Attendance> attendances = new ArrayList<Attendance>();
			  if(cursor.getCount() == 0)
			  {
				  cursor.close();
				  return attendances;
			  }
			  do
			  {
				  Attendance aAttendance = this.cursorToAttendance(cursor);
				  attendances.add(aAttendance);
			  }
			  while(cursor.moveToNext());
			  cursor.close();
			  return attendances;
		  }
		  
		  private Attendance cursorToAttendance(Cursor cursor) 
		  {
			  try 
			  {
				  	SimpleDateFormat aSimpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault());
				  	Attendance aAttendance = new Attendance();
				  	aAttendance.setId(cursor.getInt(0));
				  	aAttendance.setLockDate(aSimpleDateFormat.parse(cursor.getString(1)));
				  	aAttendance.setBalanceAmount(cursor.getInt(2));
				    aAttendance.setImage(cursor.getBlob(3));
				  	aAttendance.setTwCount(cursor.getInt(4));
				  	aAttendance.setTwCMSAmount(cursor.getDouble(5));
				  	aAttendance.setTwServiceCharge(cursor.getDouble(6));
				    Boolean myBoolean = (cursor.getInt(7) != 0);
				    aAttendance.setIsInTime(myBoolean);
				    return aAttendance;
			  } catch (ParseException e) {
			    	e.printStackTrace();
			    }
			  return null;
		  }

}
