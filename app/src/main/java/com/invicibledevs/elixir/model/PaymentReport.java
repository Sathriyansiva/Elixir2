package com.invicibledevs.elixir.model;

/**
 * Created by nandhakumarv on 05/03/16.
 */
public class PaymentReport {
    private String name;
    private String location;
    private String amountType;
    private String paymentMode;
    private String totalAmount;
    private String paidAmount;
    private String differnceAmount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(String paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getDiffernceAmount() {
        return differnceAmount;
    }

    public void setDiffernceAmount(String differnceAmount) {
        this.differnceAmount = differnceAmount;
    }
}
