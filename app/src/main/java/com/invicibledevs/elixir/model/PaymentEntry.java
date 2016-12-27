package com.invicibledevs.elixir.model;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 28/02/16.
 */
public class PaymentEntry {
    private String executiveUserId;
    private String paymentType;
    private Double chequeAmount;
    private Double cashAmount;
    private Double totalAmount;
    private Double paidAmount;
    private Double differenceAmount;
    private String remarks;
    private ArrayList<PaymentTypeDetails> paymentTypeDetailsArrayList;

    public String getExecutiveUserId() {
        return executiveUserId;
    }

    public void setExecutiveUserId(String executiveUserId) {
        this.executiveUserId = executiveUserId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Double getChequeAmount() {
        return chequeAmount;
    }

    public void setChequeAmount(Double chequeAmount) {
        this.chequeAmount = chequeAmount;
    }

    public Double getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(Double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(Double differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ArrayList<PaymentTypeDetails> getPaymentTypeDetailsArrayList() {
        return paymentTypeDetailsArrayList;
    }

    public void setPaymentTypeDetailsArrayList(ArrayList<PaymentTypeDetails> paymentTypeDetailsArrayList) {
        this.paymentTypeDetailsArrayList = paymentTypeDetailsArrayList;
    }

    private String paidTo;
    private String collectorName;
    private String collectorPassword;

    public String getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(String paidTo) {
        this.paidTo = paidTo;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public String getCollectorPassword() {
        return collectorPassword;
    }

    public void setCollectorPassword(String collectorPassword) {
        this.collectorPassword = collectorPassword;
    }
}
