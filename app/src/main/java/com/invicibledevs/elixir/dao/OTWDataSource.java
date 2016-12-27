package com.invicibledevs.elixir.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.invicibledevs.elixir.model.OTW;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 23/12/15.
 */
public class OTWDataSource {
    // OTW table name
    public static final String TABLE_OTW_NEW = "otw_new";
    // ServiceReceipt Table Columns names

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
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

    private static final String KEY_OT_ID = "otId";

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
    private static final String KEY_USER_ID = "user_id";

    private static final String KEY_IMT = "imt";
    private static final String KEY_NIL_DIP = "nil_dip";
    private static final String KEY_MANUFACTURE_YEAR = "manufacture_year";
    private static final String KEY_CLAIMS = "claims";
    private static final String KEY_NAME_TRANSFER = "name_transfer";
    private static final String KEY_INSURANCE_NO = "insurance_no";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PINCODE = "pincode";
    private static final String KEY_VEHICLE_COLOR = "vehicle_color";

    public static String CREATE_OTW_NEW_TABLE_QUERY = "CREATE TABLE " + TABLE_OTW_NEW + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_MOBILE_NO + " TEXT,"
            + KEY_ADDRESS + " TEXT," + KEY_COMPANY_NAME + " TEXT," + KEY_VEHICLE_TYPE + " TEXT," + KEY_VEHICLE_NO + " TEXT," + KEY_ENGINE_NO + " TEXT,"
            + KEY_CHASIS_NO + " TEXT," + KEY_DATE_OF_REG + " TEXT," + KEY_MAKE + " TEXT," + KEY_MODEL + " TEXT," + KEY_VARIANT + " TEXT,"
            + KEY_IDV + " TEXT," + KEY_CC + " TEXT," + KEY_IS_HAVING_INSURANCE + " INTEGER DEFAULT 0," + KEY_NCB + " TEXT," + KEY_PREVIOUS_INSURANCE + " TEXT,"
            + KEY_OD_DISCOUNT + " TEXT," + KEY_PREMIUM_AMOUNT + " TEXT," + KEY_SERVICE_CHARGE + " TEXT," + KEY_RISK_START_DATE + " TEXT," + KEY_IS_HAVING_FINANCE + " INTEGER DEFAULT 0,"
            + KEY_FINANCIER_NAME + " TEXT," + KEY_PAYMENT_TYPE + " TEXT," + KEY_POLICY_TYPE + " TEXT,"
            + KEY_IMAGE1 + " TEXT," + KEY_IMAGE2 + " TEXT," + KEY_IMAGE3 +" TEXT," + KEY_IMAGE4 +" TEXT," + KEY_IMAGE5 + " TEXT," + KEY_IMAGE6 +" TEXT,"
            + KEY_IMAGE7 +" TEXT," + KEY_IMAGE8 + " TEXT," + KEY_IMAGE9 +" TEXT," + KEY_IMAGE10 +" TEXT,"
            + KEY_DOC_IMAGE1 + " TEXT," + KEY_DOC_IMAGE2 + " TEXT," + KEY_DOC_IMAGE3 +" TEXT," + KEY_DOC_IMAGE4 +" TEXT," + KEY_DOC_IMAGE5 + " TEXT,"
            + KEY_OT_ID + " TEXT," + KEY_STATUS + " TEXT," + KEY_PDF_PATH + " TEXT," + KEY_USER_ID + " TEXT,"
            + KEY_IMT + " INTEGER DEFAULT 0," + KEY_NIL_DIP + " INTEGER DEFAULT 0," + KEY_MANUFACTURE_YEAR + " TEXT," + KEY_CLAIMS + " INTEGER DEFAULT 0,"
            + KEY_NAME_TRANSFER + " INTEGER DEFAULT 0," + KEY_INSURANCE_NO + " TEXT," + KEY_EMAIL + " TEXT," + KEY_PINCODE + " TEXT," + KEY_VEHICLE_COLOR + " TEXT)";



    public static String OTW_ID_INDEX_QUERY = "CREATE INDEX IF NOT EXISTS otwt_id_index ON " + TABLE_OTW_NEW + "(" + KEY_ID + ")";
    private String deleteQuery = "DELETE FROM " + TABLE_OTW_NEW;
    public static String OTW_ALTER_QUERY1= "ALTER TABLE " + TABLE_OTW_NEW + " ADD " + KEY_PINCODE + " TEXT";
    public static String OTW_ALTER_QUERY2= "ALTER TABLE " + TABLE_OTW_NEW + " ADD " + KEY_VEHICLE_COLOR + " TEXT";

    // Database fields
    private SQLiteDatabase database;
    private DatabaseHandler dbHelper;

    public OTWDataSource(Context context) {
        dbHelper = DatabaseHandler.getInstance(context);
    }

    private void open() throws SQLException {
        database = dbHelper.getMyWritableDatabase();
    }

    public void createOTW(OTW theOTW)
    {
        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, theOTW.getCustomerName());
        values.put(KEY_MOBILE_NO, theOTW.getMobileNo());
        values.put(KEY_ADDRESS, theOTW.getAddress());
        values.put(KEY_COMPANY_NAME, theOTW.getCompanyName());
        values.put(KEY_VEHICLE_TYPE, theOTW.getVehicleType());
        values.put(KEY_VEHICLE_NO, theOTW.getVehicleNo());
        values.put(KEY_ENGINE_NO, theOTW.getEngineNo());
        values.put(KEY_CHASIS_NO, theOTW.getChasisNo());
        values.put(KEY_DATE_OF_REG, theOTW.getDateOfReg());
        values.put(KEY_MAKE, theOTW.getMake());
        values.put(KEY_MODEL, theOTW.getModel());
        values.put(KEY_VARIANT, "");
        values.put(KEY_IDV, theOTW.getIdv()+"");
        values.put(KEY_CC, theOTW.getCc());
        values.put(KEY_IS_HAVING_INSURANCE, (theOTW.isHavingPreviousInsurance())?1:0);
        values.put(KEY_NCB, theOTW.getNcb());
        values.put(KEY_PREVIOUS_INSURANCE, theOTW.getPreviousInsurance());
        values.put(KEY_OD_DISCOUNT, theOTW.getOdDiscount()+"");
        values.put(KEY_PREMIUM_AMOUNT, theOTW.getPreminumAmount()+"");
        values.put(KEY_SERVICE_CHARGE, theOTW.getServiceCharege()+"");
        values.put(KEY_PREVIOUS_INSURANCE, theOTW.getPreviousInsurance());
        values.put(KEY_RISK_START_DATE, theOTW.getRiskStartDate());
        values.put(KEY_IS_HAVING_FINANCE, (theOTW.isHavingFinance())?1:0);
        values.put(KEY_FINANCIER_NAME, theOTW.getFinancierName());
        values.put(KEY_PAYMENT_TYPE, theOTW.getPaymentType());
        values.put(KEY_POLICY_TYPE, theOTW.getPolicyType());

        values.put(KEY_IMAGE1, theOTW.getImage1());
        values.put(KEY_IMAGE2, theOTW.getImage2());
        values.put(KEY_IMAGE3, theOTW.getImage3());
        values.put(KEY_IMAGE4, theOTW.getImage4());
        values.put(KEY_IMAGE5, theOTW.getImage5());
        values.put(KEY_IMAGE6, theOTW.getImage6());
        values.put(KEY_IMAGE7, theOTW.getImage7());
        values.put(KEY_IMAGE8, theOTW.getImage8());
        values.put(KEY_IMAGE9, theOTW.getImage9());
        values.put(KEY_IMAGE10, theOTW.getImage10());
        values.put(KEY_DOC_IMAGE1, theOTW.getDocImage1());
        values.put(KEY_DOC_IMAGE2, theOTW.getDocImage2());
        values.put(KEY_DOC_IMAGE3, theOTW.getDocImage3());
        values.put(KEY_DOC_IMAGE4, theOTW.getDocImage4());
        values.put(KEY_DOC_IMAGE5, theOTW.getDocImage5());
        values.put(KEY_OT_ID, theOTW.getOtwId());
        values.put(KEY_STATUS, theOTW.getStatus());
        values.put(KEY_PDF_PATH, theOTW.getPdfPath());
        values.put(KEY_USER_ID, theOTW.getUserId());

        values.put(KEY_IMT, (theOTW.isImtStatus())?1:0);
        values.put(KEY_NIL_DIP, (theOTW.isNilDIPStatus())?1:0);
        values.put(KEY_MANUFACTURE_YEAR, theOTW.getYearOfManufacture());
        values.put(KEY_CLAIMS, (theOTW.isClaimsStatus())?1:0);
        values.put(KEY_NAME_TRANSFER, (theOTW.isNameTransferStatus())?1:0);
        values.put(KEY_INSURANCE_NO, theOTW.getPreviousInsuranceNo());
        values.put(KEY_EMAIL, theOTW.getEmailId());
        values.put(KEY_PINCODE, theOTW.getPinCode());
        values.put(KEY_VEHICLE_COLOR, theOTW.getVehicleColor());
        // Inserting Row
        long rowId = database.insert(TABLE_OTW_NEW, null, values);
        theOTW.setId((int) rowId);
    }

    /**
     * Clears the table
     */
    public void clearOTW()
    {
        this.open();
        database.execSQL(deleteQuery);
    }



    public ArrayList<OTW> getAllOTW()
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_OTW_NEW ;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<OTW> otws = new ArrayList<OTW>();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return otws;
        }
        do
        {
            OTW aOtw = this.cursorToOTW(cursor);
            otws.add(aOtw);
        }
        while(cursor.moveToNext());
        cursor.close();
        return otws;
    }

    public OTW getOTWById(String otId)
    {
        this.open();
        String selectQuery = "SELECT  * FROM " + TABLE_OTW_NEW +" where " + KEY_OT_ID + " = " + otId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0)
        {
            cursor.close();
            return null;
        }
        OTW aOtw = this.cursorToOTW(cursor);


        cursor.close();
        return aOtw;
    }

    public void deleteOTWById(String otId)
    {
        this.open();
        String selectQuery = "DELETE FROM " + TABLE_OTW_NEW +" where " + KEY_OT_ID + " = " + otId;
        database.execSQL(selectQuery);
    }

    public void updateOTW(OTW theOtw)
    {
        this.open();

        this.open();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, theOtw.getStatus());
        values.put(KEY_PDF_PATH, theOtw.getPdfPath());
        // Updating Row
        database.update(TABLE_OTW_NEW, values, KEY_ID+"="+theOtw.getId(), null);
    }

    private OTW cursorToOTW(Cursor cursor)
    {
        try
        {
            OTW aOTW = new OTW();
            aOTW.setId(cursor.getInt(0));
            aOTW.setCustomerName(cursor.getString(1));
            aOTW.setMobileNo(cursor.getString(2));
            aOTW.setAddress(cursor.getString(3));
            aOTW.setCompanyName(cursor.getString(4));
            aOTW.setVehicleType(cursor.getString(5));
            aOTW.setVehicleNo(cursor.getString(6));
            aOTW.setEngineNo(cursor.getString(7));
            aOTW.setChasisNo(cursor.getString(8));
            aOTW.setDateOfReg(cursor.getString(9));
            aOTW.setMake(cursor.getString(10));
            aOTW.setModel(cursor.getString(11));
            aOTW.setIdv(cursor.getDouble(13));
            aOTW.setCc(cursor.getString(14));
            aOTW.setIsHavingPreviousInsurance((cursor.getInt(15) == 1) ? true : false);
            aOTW.setNcb(cursor.getString(16));
            aOTW.setPreviousInsurance(cursor.getString(17));
            aOTW.setOdDiscount(cursor.getInt(18));
            aOTW.setPreminumAmount(cursor.getDouble(19));
            aOTW.setServiceCharege(cursor.getDouble(20));
            aOTW.setRiskStartDate(cursor.getString(21));
            aOTW.setIsHavingFinance((cursor.getInt(22) == 1) ? true : false);
            aOTW.setFinancierName(cursor.getString(23));
            aOTW.setPaymentType(cursor.getString(24));
            aOTW.setPolicyType(cursor.getString(25));
            aOTW.setImage1(cursor.getBlob(26));
            aOTW.setImage2(cursor.getBlob(27));
            aOTW.setImage3(cursor.getBlob(28));
            aOTW.setImage4(cursor.getBlob(29));
            aOTW.setImage5(cursor.getBlob(30));
            aOTW.setImage6(cursor.getBlob(31));
            aOTW.setImage7(cursor.getBlob(32));
            aOTW.setImage8(cursor.getBlob(33));
            aOTW.setImage9(cursor.getBlob(34));
            aOTW.setImage10(cursor.getBlob(35));
            aOTW.setDocImage1(cursor.getBlob(36));
            aOTW.setDocImage2(cursor.getBlob(37));
            aOTW.setDocImage3(cursor.getBlob(38));
            aOTW.setDocImage4(cursor.getBlob(39));
            aOTW.setDocImage5(cursor.getBlob(40));
            aOTW.setOtwId(cursor.getString(41));
            aOTW.setStatus(cursor.getString(42));
            aOTW.setPdfPath(cursor.getString(43));
            aOTW.setUserId(cursor.getString(44));
            aOTW.setImtStatus((cursor.getInt(45) == 1) ? true : false);
            aOTW.setNilDIPStatus((cursor.getInt(46) == 1) ? true : false);
            aOTW.setYearOfManufacture(cursor.getString(47));
            aOTW.setClaimsStatus((cursor.getInt(48) == 1) ? true : false);
            aOTW.setNameTransferStatus((cursor.getInt(49) == 1) ? true : false);
            aOTW.setPreviousInsuranceNo(cursor.getString(50));
            aOTW.setEmailId(cursor.getString(51));
            aOTW.setPinCode(cursor.getString(52));
            aOTW.setVehicleColor(cursor.getString(53));
            return aOTW;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
