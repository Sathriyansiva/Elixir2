package com.invicibledevs.elixir.model;

import java.util.Date;

/**
 * Created by nandhakumarv on 16/12/15.
 */
public class Leads {

    private int id;
    private String name;
    private String vehicleType;
    private String vehicleNo;
    private double idv;
    private String previousInsurance;
    private String mobileNo;
    private String riskEndDateString;
    private Date riskEndDate;
    private String ncb;
    private String email;
    private String yom;
    private String make;
    private String model;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public double getIdv() {
        return idv;
    }

    public void setIdv(double idv) {
        this.idv = idv;
    }

    public String getPreviousInsurance() {
        return previousInsurance;
    }

    public void setPreviousInsurance(String previousInsurance) {
        this.previousInsurance = previousInsurance;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getRiskEndDateString() {
        return riskEndDateString;
    }

    public void setRiskEndDateString(String riskEndDateString) {
        this.riskEndDateString = riskEndDateString;
    }

    public Date getRiskEndDate() {
        return riskEndDate;
    }

    public void setRiskEndDate(Date riskEndDate) {
        this.riskEndDate = riskEndDate;
    }

    public String getNcb() {
        return ncb;
    }

    public void setNcb(String ncb) {
        this.ncb = ncb;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getYom() {
        return yom;
    }

    public void setYom(String yom) {
        this.yom = yom;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
