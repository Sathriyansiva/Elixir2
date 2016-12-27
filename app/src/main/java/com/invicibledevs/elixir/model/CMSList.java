package com.invicibledevs.elixir.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nandhakumarv on 21/01/16.
 */
public class CMSList implements Serializable {
    private int id;
    private int amountType;
    private Date cmsDate;
    private String cmsDateStr;
    private String cmsVehiNo;
    private String cmsVehiType;
    private String cmsAmount;
    private String status;
    private boolean isSelected;
    private static final long serialVersionUID = 46543445;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCmsDate() {
        return cmsDate;
    }

    public void setCmsDate(Date cmsDate) {
        this.cmsDate = cmsDate;
    }

    public String getCmsDateStr() {
        return cmsDateStr;
    }

    public void setCmsDateStr(String cmsDateStr) {
        this.cmsDateStr = cmsDateStr;
    }

    public String getCmsVehiNo() {
        return cmsVehiNo;
    }

    public void setCmsVehiNo(String cmsVehiNo) {
        this.cmsVehiNo = cmsVehiNo;
    }

    public String getCmsVehiType() {
        return cmsVehiType;
    }

    public void setCmsVehiType(String cmsVehiType) {
        this.cmsVehiType = cmsVehiType;
    }

    public String getCmsAmount() {
        return cmsAmount;
    }

    public void setCmsAmount(String cmsAmount) {
        this.cmsAmount = cmsAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getAmountType() {
        return amountType;
    }

    public void setAmountType(int amountType) {
        this.amountType = amountType;
    }
}
