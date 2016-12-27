package com.invicibledevs.elixir.model;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 23/02/16.
 */
public class Payment {
    private String name;
    private String id;
    private double service;
    private double cms;
    private double otw;

    private ArrayList<PaymentTypeDetails> serviceList;
    private ArrayList<PaymentTypeDetails> cmsList;
    private ArrayList<PaymentTypeDetails> otwList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getService() {
        return service;
    }

    public void setService(double service) {
        this.service = service;
    }

    public double getCms() {
        return cms;
    }

    public void setCms(double cms) {
        this.cms = cms;
    }

    public double getOtw() {
        return otw;
    }

    public void setOtw(double otw) {
        this.otw = otw;
    }

    public ArrayList<PaymentTypeDetails> getServiceList() {
        return serviceList;
    }

    public void setServiceList(ArrayList<PaymentTypeDetails> serviceList) {
        this.serviceList = serviceList;
    }

    public ArrayList<PaymentTypeDetails> getCmsList() {
        return cmsList;
    }

    public void setCmsList(ArrayList<PaymentTypeDetails> cmsList) {
        this.cmsList = cmsList;
    }

    public ArrayList<PaymentTypeDetails> getOtwList() {
        return otwList;
    }

    public void setOtwList(ArrayList<PaymentTypeDetails> otwList) {
        this.otwList = otwList;
    }
}
