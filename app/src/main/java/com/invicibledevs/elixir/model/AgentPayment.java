package com.invicibledevs.elixir.model;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 08/04/16.
 */
public class AgentPayment {
    private String unreachedAmount;
    private String id;
    private String totalOutstandingAmount;

    private ArrayList<PaymentTypeDetails> detailsList;
    private ArrayList<Role> paidUserList;

    public String getUnreachedAmount() {
        return unreachedAmount;
    }

    public void setUnreachedAmount(String unreachedAmount) {
        this.unreachedAmount = unreachedAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTotalOutstandingAmount() {
        return totalOutstandingAmount;
    }

    public void setTotalOutstandingAmount(String totalOutstandingAmount) {
        this.totalOutstandingAmount = totalOutstandingAmount;
    }

    public ArrayList<PaymentTypeDetails> getDetailsList() {
        return detailsList;
    }

    public void setDetailsList(ArrayList<PaymentTypeDetails> detailsList) {
        this.detailsList = detailsList;
    }

    public ArrayList<Role> getPaidUserList() {
        return paidUserList;
    }

    public void setPaidUserList(ArrayList<Role> paidUserList) {
        this.paidUserList = paidUserList;
    }


}
