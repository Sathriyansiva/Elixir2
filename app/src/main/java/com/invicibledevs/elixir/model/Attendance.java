package com.invicibledevs.elixir.model;

import java.util.Date;

/**
 * Created by nandhakumarv on 14/11/15.
 */
public class Attendance {
    private int id;
    private double balanceAmount;
    private byte[] image;
    private int twCount;
    private double twCMSAmount;
    private boolean isInTime;

    public String getLockDateString() {
        return lockDateString;
    }

    public void setLockDateString(String lockDateString) {
        this.lockDateString = lockDateString;
    }

    private String lockDateString;

    public boolean isInTime() {
        return isInTime;
    }

    public void setIsInTime(boolean isInTime) {
        this.isInTime = isInTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(double balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getTwCount() {
        return twCount;
    }

    public void setTwCount(int twCount) {
        this.twCount = twCount;
    }

    public double getTwCMSAmount() {
        return twCMSAmount;
    }

    public void setTwCMSAmount(double twCMSAmount) {
        this.twCMSAmount = twCMSAmount;
    }

    public double getTwServiceCharge() {
        return twServiceCharge;
    }

    public void setTwServiceCharge(double twServiceCharge) {
        this.twServiceCharge = twServiceCharge;
    }

    public Date getLockDate() {
        return lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    private double twServiceCharge;
    private Date lockDate;
}
