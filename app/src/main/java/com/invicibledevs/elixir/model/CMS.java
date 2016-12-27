package com.invicibledevs.elixir.model;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 22/01/16.
 */
public class CMS {

    private int id;
    private double totalCMSAmount;
    private double paidAmount;
    private String transactionId;
    private String remarks;
    private String paidBy;
    private String paidDate;
    private String paidAccNo;
    private String cashcollName;
    private String staffName;
    private String paymentMode;
    private String otp;
    private ArrayList<CMSList> cmsLists;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTotalCMSAmount() {
        return totalCMSAmount;
    }

    public void setTotalCMSAmount(double totalCMSAmount) {
        this.totalCMSAmount = totalCMSAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ArrayList<CMSList> getCmsLists() {
        return cmsLists;
    }

    public void setCmsLists(ArrayList<CMSList> cmsLists) {
        this.cmsLists = cmsLists;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public String getPaidAccNo() {
        return paidAccNo;
    }

    public void setPaidAccNo(String paidAccNo) {
        this.paidAccNo = paidAccNo;
    }

    public String getCashcollName() {
        return cashcollName;
    }

    public void setCashcollName(String cashcollName) {
        this.cashcollName = cashcollName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
