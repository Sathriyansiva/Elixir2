package com.invicibledevs.elixir.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.invicibledevs.elixir.model.User;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 29/11/15.
 */
public class UserDataSource {
    // User table name
    public static final String TABLE_USER = "user";
    // Attendance Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_USER_ID = "user_id";
    private static final String KEY_BRANCH_ID = "branch_id";
    private static final String KEY_LOCATION_ID = "location_id";
    private static final String KEY_TEAM_LEADER_ID = "team_leader_id";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_AREA_NAME = "area_name";
    public static final String KEY_EXECUTIVE_NAME = "executive_name";
    private static final String KEY_GENERAL_NEWS = "general_news";
    public static final String KEY_PERSONAL_NEWS = "personal_news";
    private static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_SERVICE_CHARGE = "service_charge";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_ROLE_ID = "role_id";

    public static String CREATE_USER_TABLE_QUERY = "CREATE TABLE " + TABLE_USER + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_ID + " TEXT,"
            + KEY_BRANCH_ID + " TEXT," + KEY_LOCATION_ID + " TEXT," + KEY_TEAM_LEADER_ID + " TEXT,"
            + KEY_LOCATION_NAME + " TEXT," + KEY_AREA_NAME + " TEXT," + KEY_EXECUTIVE_NAME + " TEXT,"
            + KEY_GENERAL_NEWS + " TEXT," + KEY_PERSONAL_NEWS + " TEXT," + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT,"
            + KEY_SERVICE_CHARGE + " TEXT," + KEY_DETAILS + " TEXT," + KEY_ROLE_ID + " TEXT)";

    public static String USER_ID_INDEX_QUERY = "CREATE INDEX IF NOT EXISTS user_id_index ON " + TABLE_USER + "(" + KEY_ID + ")";
    private String deleteQuery = "DELETE FROM " + TABLE_USER;

    public static String USER_ALTER_QUERY1 = "ALTER TABLE " + TABLE_USER + " ADD " + KEY_DETAILS + " TEXT";
    public static String USER_ALTER_QUERY2 = "ALTER TABLE " + TABLE_USER + " ADD " + KEY_ROLE_ID + " TEXT";

    // Database fields
    private SQLiteDatabase database;
    private DatabaseHandler dbHelper;

    public UserDataSource(Context context) {
        dbHelper = DatabaseHandler.getInstance(context);
    }

    private void open() throws SQLException {
        database = dbHelper.getMyWritableDatabase();
    }

    public void createUser(User theUser)
    {
        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, theUser.getUserId());
        values.put(KEY_BRANCH_ID, theUser.getBranchId());
        values.put(KEY_LOCATION_ID, theUser.getLocationId());
        values.put(KEY_TEAM_LEADER_ID, theUser.getTeamLeaderId());
        values.put(KEY_LOCATION_NAME, theUser.getLocationName());
        values.put(KEY_AREA_NAME, theUser.getAreaName());
        values.put(KEY_EXECUTIVE_NAME, theUser.getExecutiveName());
        values.put(KEY_GENERAL_NEWS, theUser.getGeneralNews());
        values.put(KEY_PERSONAL_NEWS, theUser.getPersonalNews());
        values.put(KEY_LATITUDE, theUser.getLatitude());
        values.put(KEY_LONGITUDE, theUser.getLongitude());
        values.put(KEY_SERVICE_CHARGE, theUser.getServiceCharge());
        values.put(KEY_DETAILS, theUser.getDetails());
        values.put(KEY_ROLE_ID, theUser.getRoleId());

        // Inserting Row
        long rowId = database.insert(TABLE_USER, null, values);
        theUser.setId((int) rowId);
    }

    /**
     * Clears the table
     */
    public void clearUser()
    {
        this.open();
        database.execSQL(deleteQuery);
    }



    public ArrayList<User> getAllUser()
    {
        this.open();
        String selectByUserNameQuery = "SELECT  * FROM " + TABLE_USER ;
        Cursor cursor = database.rawQuery(selectByUserNameQuery, null);
        cursor.moveToFirst();
        ArrayList<User> users = new ArrayList<User>();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return users;
        }
        do
        {
            User aUser = this.cursorToUser(cursor);
            users.add(aUser);
        }
        while(cursor.moveToNext());
        cursor.close();
        return users;
    }

    public User getUserByUserId(String userId)
    {
        this.open();
        String selectByUserNameQuery = "SELECT  * FROM " + TABLE_USER + " where " + KEY_USER_ID + " = " + userId;
        Cursor cursor = database.rawQuery(selectByUserNameQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        }
        User aUser = this.cursorToUser(cursor);
        cursor.close();
        return aUser;
    }

    public void deleteUserByUserId(String userId)
    {
        this.open();
        String deleteByUserIdQuery = "DELETE FROM " + TABLE_USER + " where " + KEY_USER_ID + " = " + userId;
        database.execSQL(deleteByUserIdQuery);

    }

    private User cursorToUser(Cursor cursor)
    {
        try
        {

            User aUser = new User();
            aUser.setId(cursor.getInt(0));
            aUser.setUserId(cursor.getString(1));
            aUser.setBranchId(cursor.getString(2));
            aUser.setLocationId(cursor.getString(3));
            aUser.setTeamLeaderId(cursor.getString(4));
            aUser.setLocationName(cursor.getString(5));
            aUser.setAreaName(cursor.getString(6));
            aUser.setExecutiveName(cursor.getString(7));
            aUser.setGeneralNews(cursor.getString(8));
            aUser.setPersonalNews(cursor.getString(9));
            aUser.setLatitude(cursor.getString(10));
            aUser.setLongitude(cursor.getString(11));
            aUser.setServiceCharge(cursor.getString(12));
            aUser.setDetails(cursor.getString(13));
            aUser.setRoleId(cursor.getString(14));
            return aUser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
