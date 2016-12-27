package com.invicibledevs.elixir.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.invicibledevs.elixir.model.Policy;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 06/04/16.
 */
public class PolicyDataSource {
    // Policy table name
    public static final String TABLE_POLICY = "policy";
    // Policy Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_CUSTOMER_NAME = "customer_name";
    private static final String KEY_MOBILE_NO = "mobile_no";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_COMPANY_NAME = "company_name";
    private static final String KEY_VEHICLE_TYPE = "vehicle_type";
    private static final String KEY_VEHICLE_NO = "vehicle_no";
    private static final String KEY_ENGINE_NO = "engine_no";
    private static final String KEY_CHASIS_NO = "chasis_no";
    private static final String KEY_DATE_OF_REG = "date_of_reg";
    private static final String KEY_MAKE = "make";
    private static final String KEY_MODEL = "model";
    private static final String KEY_VARIANT = "variant";
    private static final String KEY_IDV = "idv";
    private static final String KEY_CC = "cc";
    private static final String KEY_IS_HAVING_INSURANCE = "is_having_insurance";
    private static final String KEY_NCB = "ncb";
    private static final String KEY_PREVIOUS_INSURANCE = "previous_insurance";
    private static final String KEY_OD_DISCOUNT = "od_discount";
    private static final String KEY_PREMIUM_AMOUNT = "premium_amount";
    private static final String KEY_SERVICE_CHARGE = "service_charge";
    private static final String KEY_RISK_START_DATE = "risk_start_date";
    private static final String KEY_IS_HAVING_FINANCE = "is_having_finance";
    private static final String KEY_FINANCIER_NAME = "financier_name";
    private static final String KEY_PAYMENT_TYPE = "payment_type";
    private static final String KEY_POLICY_TYPE = "policy_type";

    private static final String KEY_POLICY_ID = "policy_id";

    private static final String KEY_IMAGE1 = "image1";
    private static final String KEY_IMAGE2 = "image2";
    private static final String KEY_IMAGE3 = "image3";
    private static final String KEY_IMAGE4 = "image4";
    private static final String KEY_IMAGE5 = "image5";
    private static final String KEY_IMAGE6 = "image6";
    private static final String KEY_IMAGE7 = "image7";
    private static final String KEY_IMAGE8 = "image8";
    private static final String KEY_IMAGE9 = "image9";
    private static final String KEY_IMAGE10 = "image10";

    private static final String KEY_DOC_IMAGE1 = "doc_image1";
    private static final String KEY_DOC_IMAGE2 = "doc_image2";
    private static final String KEY_DOC_IMAGE3 = "doc_image3";
    private static final String KEY_DOC_IMAGE4 = "doc_image4";
    private static final String KEY_DOC_IMAGE5 = "doc_image5";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PDF_PATH = "pdf_path";
    private static final String KEY_POLICY_PREPARE_BY = "policy_prepare_by";
    private static final String KEY_USER_ID = "user_id";

    private static final String KEY_IMT = "imt";
    private static final String KEY_NIL_DIP = "nil_dip";
    private static final String KEY_MANUFACTURE_YEAR = "manufacture_year";
    private static final String KEY_CLAIMS = "claims";
    private static final String KEY_NAME_TRANSFER = "name_transfer";
    private static final String KEY_INSURANCE_NO = "insurance_no";
    private static final String KEY_EMAIL = "email";


    public static String CREATE_POLICY_TABLE_QUERY = "CREATE TABLE " + TABLE_POLICY + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CUSTOMER_NAME + " TEXT," + KEY_MOBILE_NO + " TEXT,"
            + KEY_ADDRESS + " TEXT," + KEY_COMPANY_NAME + " TEXT," + KEY_VEHICLE_TYPE + " TEXT," + KEY_VEHICLE_NO + " TEXT," + KEY_ENGINE_NO + " TEXT,"
            + KEY_CHASIS_NO + " TEXT," + KEY_DATE_OF_REG + " TEXT," + KEY_MAKE + " TEXT," + KEY_MODEL + " TEXT," + KEY_VARIANT + " TEXT,"
            + KEY_IDV + " TEXT," + KEY_CC + " TEXT," + KEY_IS_HAVING_INSURANCE + " INTEGER DEFAULT 0," + KEY_NCB + " TEXT," + KEY_PREVIOUS_INSURANCE + " TEXT,"
            + KEY_OD_DISCOUNT + " TEXT," + KEY_PREMIUM_AMOUNT + " TEXT," + KEY_SERVICE_CHARGE + " TEXT," + KEY_RISK_START_DATE + " TEXT," + KEY_IS_HAVING_FINANCE + " INTEGER DEFAULT 0,"
            + KEY_FINANCIER_NAME + " TEXT," + KEY_PAYMENT_TYPE + " TEXT," + KEY_POLICY_TYPE + " TEXT,"
            + KEY_IMAGE1 + " TEXT," + KEY_IMAGE2 + " TEXT," + KEY_IMAGE3 +" TEXT," + KEY_IMAGE4 +" TEXT," + KEY_IMAGE5 + " TEXT," + KEY_IMAGE6 +" TEXT,"
            + KEY_IMAGE7 +" TEXT," + KEY_IMAGE8 + " TEXT," + KEY_IMAGE9 +" TEXT," + KEY_IMAGE10 +" TEXT,"
            + KEY_DOC_IMAGE1 + " TEXT," + KEY_DOC_IMAGE2 + " TEXT," + KEY_DOC_IMAGE3 +" TEXT," + KEY_DOC_IMAGE4 +" TEXT," + KEY_DOC_IMAGE5 + " TEXT,"
            + KEY_POLICY_ID + " TEXT," + KEY_STATUS + " TEXT," + KEY_PDF_PATH + " TEXT," + KEY_POLICY_PREPARE_BY + " TEXT," + KEY_USER_ID + " TEXT,"
            + KEY_IMT + " INTEGER DEFAULT 0," + KEY_NIL_DIP + " INTEGER DEFAULT 0," + KEY_MANUFACTURE_YEAR + " TEXT," + KEY_CLAIMS + " INTEGER DEFAULT 0,"
            + KEY_NAME_TRANSFER + " INTEGER DEFAULT 0," + KEY_INSURANCE_NO + " TEXT," + KEY_EMAIL + " TEXT)";


    public static String POLICY_ID_INDEX_QUERY = "CREATE INDEX IF NOT EXISTS policy_id_index ON " + TABLE_POLICY + "(" + KEY_ID + ")";
    private String deleteQuery = "DELETE FROM " + TABLE_POLICY;

    public static String POLICY_ALTER_QUERY1 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_IMT + " INTEGER DEFAULT 0";
    public static String POLICY_ALTER_QUERY2 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_NIL_DIP + " INTEGER DEFAULT 0";
    public static String POLICY_ALTER_QUERY3 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_MANUFACTURE_YEAR + " TEXT";
    public static String POLICY_ALTER_QUERY4 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_CLAIMS + " INTEGER DEFAULT 0";
    public static String POLICY_ALTER_QUERY5 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_NAME_TRANSFER + " INTEGER DEFAULT 0";
    public static String POLICY_ALTER_QUERY6 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_INSURANCE_NO + " TEXT";
    public static String POLICY_ALTER_QUERY7 = "ALTER TABLE " + TABLE_POLICY + " ADD " + KEY_EMAIL + " TEXT";


    // Database fields
    private SQLiteDatabase database;
    private DatabaseHandler dbHelper;

    public PolicyDataSource(Context context) {
        dbHelper = DatabaseHandler.getInstance(context);
    }

    private void open() throws SQLException {
        database = dbHelper.getMyWritableDatabase();
    }

    public void createPolicy(Policy thePolicy)
    {
        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_NAME, thePolicy.getCustomerName());
        values.put(KEY_MOBILE_NO, thePolicy.getMobileNo());
        values.put(KEY_ADDRESS, thePolicy.getAddress());
        values.put(KEY_COMPANY_NAME, thePolicy.getCompanyName());
        values.put(KEY_VEHICLE_TYPE, thePolicy.getVehicleType());
        values.put(KEY_VEHICLE_NO, thePolicy.getVehicleNo());
        values.put(KEY_ENGINE_NO, thePolicy.getEngineNo());
        values.put(KEY_CHASIS_NO, thePolicy.getChasisNo());
        values.put(KEY_DATE_OF_REG, thePolicy.getDateOfReg());
        values.put(KEY_MAKE, thePolicy.getMake());
        values.put(KEY_MODEL, thePolicy.getModel());
        values.put(KEY_VARIANT, "");
        values.put(KEY_IDV, thePolicy.getIdv()+"");
        values.put(KEY_CC, thePolicy.getCc());
        values.put(KEY_IS_HAVING_INSURANCE, (thePolicy.isHavingPreviousInsurance())?1:0);
        values.put(KEY_NCB, thePolicy.getNcb());
        values.put(KEY_PREVIOUS_INSURANCE, thePolicy.getPreviousInsurance());
        values.put(KEY_OD_DISCOUNT, thePolicy.getOdDiscount()+"");
        values.put(KEY_PREMIUM_AMOUNT, thePolicy.getPreminumAmount()+"");
        values.put(KEY_SERVICE_CHARGE, thePolicy.getServiceCharege()+"");
        values.put(KEY_PREVIOUS_INSURANCE, thePolicy.getPreviousInsurance());
        values.put(KEY_RISK_START_DATE, thePolicy.getRiskStartDate());
        values.put(KEY_IS_HAVING_FINANCE, (thePolicy.isHavingFinance())?1:0);
        values.put(KEY_FINANCIER_NAME, thePolicy.getFinancierName());
        values.put(KEY_PAYMENT_TYPE, thePolicy.getPaymentType());
        values.put(KEY_POLICY_TYPE, thePolicy.getPolicyType());

        values.put(KEY_IMAGE1, thePolicy.getImage1());
        values.put(KEY_IMAGE2, thePolicy.getImage2());
        values.put(KEY_IMAGE3, thePolicy.getImage3());
        values.put(KEY_IMAGE4, thePolicy.getImage4());
        values.put(KEY_IMAGE5, thePolicy.getImage5());
        values.put(KEY_IMAGE6, thePolicy.getImage6());
        values.put(KEY_IMAGE7, thePolicy.getImage7());
        values.put(KEY_IMAGE8, thePolicy.getImage8());
        values.put(KEY_IMAGE9, thePolicy.getImage9());
        values.put(KEY_IMAGE10, thePolicy.getImage10());
        values.put(KEY_DOC_IMAGE1, thePolicy.getDocImage1());
        values.put(KEY_DOC_IMAGE2, thePolicy.getDocImage2());
        values.put(KEY_DOC_IMAGE3, thePolicy.getDocImage3());
        values.put(KEY_DOC_IMAGE4, thePolicy.getDocImage4());
        values.put(KEY_DOC_IMAGE5, thePolicy.getDocImage5());
        values.put(KEY_POLICY_ID, thePolicy.getPolicyId());
        values.put(KEY_STATUS, thePolicy.getStatus());
        values.put(KEY_PDF_PATH, thePolicy.getPdfPath());
        values.put(KEY_POLICY_PREPARE_BY, thePolicy.getPolicyPreparedBy());
        values.put(KEY_USER_ID, thePolicy.getUserId());

        values.put(KEY_IMT, (thePolicy.isImtStatus())?1:0);
        values.put(KEY_NIL_DIP, (thePolicy.isNilDIPStatus())?1:0);
        values.put(KEY_MANUFACTURE_YEAR, thePolicy.getYearOfManufacture());
        values.put(KEY_CLAIMS, (thePolicy.isClaimsStatus())?1:0);
        values.put(KEY_NAME_TRANSFER, (thePolicy.isNameTransferStatus())?1:0);
        values.put(KEY_INSURANCE_NO, thePolicy.getPreviousInsuranceNo());
        values.put(KEY_EMAIL, thePolicy.getEmailId());

        // Inserting Row
        long rowId = database.insert(TABLE_POLICY, null, values);
        thePolicy.setId((int) rowId);
    }

    /**
     * Clears the table
     */
    public void clearPolicy()
    {
        this.open();
        database.execSQL(deleteQuery);
    }



    public ArrayList<Policy> getAllPolicy()
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_POLICY ;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<Policy> policies = new ArrayList<Policy>();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return policies;
        }
        do
        {
            Policy aPolicy = this.cursorToPolicy(cursor);
            policies.add(aPolicy);
        }
        while(cursor.moveToNext());
        cursor.close();
        return policies;
    }

    public ArrayList<Policy> getAllPolicyByUserId(String userId)
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_POLICY +" where " + KEY_USER_ID + " = " + userId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<Policy> policies = new ArrayList<Policy>();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return policies;
        }
        do
        {
            Policy aPolicy = this.cursorToPolicy(cursor);
            policies.add(aPolicy);
        }
        while(cursor.moveToNext());
        cursor.close();
        return policies;
    }

    public Policy getPolicyById(String policyId)
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_POLICY +" where " + KEY_POLICY_ID + " = " + policyId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        }
        Policy aPolicy = this.cursorToPolicy(cursor);


        cursor.close();
        return aPolicy;
    }

    public void deletePolicyById(String policyID)
    {
        this.open();
        String selectQuery = "DELETE FROM " + TABLE_POLICY +" where " + KEY_POLICY_ID + " = " + policyID;
        database.execSQL(selectQuery);
    }

    public void updatePolicy(Policy thePolicy)
    {
        this.open();

        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, thePolicy.getStatus());
        values.put(KEY_PDF_PATH, thePolicy.getPdfPath());
        // Updating Row
        database.update(TABLE_POLICY, values, KEY_ID+"="+thePolicy.getId(), null);
    }

    private Policy cursorToPolicy(Cursor cursor)
    {
        try
        {
            Policy aPolicy = new Policy();
            aPolicy.setId(cursor.getInt(0));
            aPolicy.setCustomerName(cursor.getString(1));
            aPolicy.setMobileNo(cursor.getString(2));
            aPolicy.setAddress(cursor.getString(3));
            aPolicy.setCompanyName(cursor.getString(4));
            aPolicy.setVehicleType(cursor.getString(5));
            aPolicy.setVehicleNo(cursor.getString(6));
            aPolicy.setEngineNo(cursor.getString(7));
            aPolicy.setChasisNo(cursor.getString(8));
            aPolicy.setDateOfReg(cursor.getString(9));
            aPolicy.setMake(cursor.getString(10));
            aPolicy.setModel(cursor.getString(11));
//            aPolicy.setVariant(cursor.getString(12));
            aPolicy.setIdv(cursor.getDouble(13));
            aPolicy.setCc(cursor.getString(14));
            aPolicy.setIsHavingPreviousInsurance((cursor.getInt(15) == 1) ? true : false);
            aPolicy.setNcb(cursor.getString(16));
            aPolicy.setPreviousInsurance(cursor.getString(17));
            aPolicy.setOdDiscount(cursor.getInt(18));
            aPolicy.setPreminumAmount(cursor.getDouble(19));
            aPolicy.setServiceCharege(cursor.getDouble(20));
            aPolicy.setRiskStartDate(cursor.getString(21));
            aPolicy.setIsHavingFinance((cursor.getInt(22) == 1) ? true : false);
            aPolicy.setFinancierName(cursor.getString(23));
            aPolicy.setPaymentType(cursor.getString(24));
            aPolicy.setPolicyType(cursor.getString(25));

            aPolicy.setImage1(cursor.getBlob(26));
            aPolicy.setImage2(cursor.getBlob(27));
            aPolicy.setImage3(cursor.getBlob(28));
            aPolicy.setImage4(cursor.getBlob(29));
            aPolicy.setImage5(cursor.getBlob(30));
            aPolicy.setImage6(cursor.getBlob(31));
            aPolicy.setImage7(cursor.getBlob(32));
            aPolicy.setImage8(cursor.getBlob(33));
            aPolicy.setImage9(cursor.getBlob(34));
            aPolicy.setImage10(cursor.getBlob(35));
            aPolicy.setDocImage1(cursor.getBlob(36));
            aPolicy.setDocImage2(cursor.getBlob(37));
            aPolicy.setDocImage3(cursor.getBlob(38));
            aPolicy.setDocImage4(cursor.getBlob(39));
            aPolicy.setDocImage5(cursor.getBlob(40));
            aPolicy.setPolicyId(cursor.getString(41));
            aPolicy.setStatus(cursor.getString(42));
            aPolicy.setPdfPath(cursor.getString(43));
            aPolicy.setPolicyPreparedBy(cursor.getString(44));
            aPolicy.setUserId(cursor.getString(45));

            aPolicy.setImtStatus((cursor.getInt(46) == 1) ? true : false);
            aPolicy.setNilDIPStatus((cursor.getInt(47) == 1) ? true : false);
            aPolicy.setYearOfManufacture(cursor.getString(48));
            aPolicy.setClaimsStatus((cursor.getInt(49) == 1) ? true : false);
            aPolicy.setNameTransferStatus((cursor.getInt(50) == 1) ? true : false);
            aPolicy.setPreviousInsuranceNo(cursor.getString(51));
            aPolicy.setEmailId(cursor.getString(52));

            return aPolicy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
