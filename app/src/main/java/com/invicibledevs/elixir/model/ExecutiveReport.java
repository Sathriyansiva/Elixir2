package com.invicibledevs.elixir.model;

/**
 * Created by admin on 11/8/2016.
 */
public class ExecutiveReport {
    private String date;
    private String intime;
    private String outtime;
    private String policycount;
    private String leadscount;
    private String otwcount;

    public String getdate() {
        return date;
    }

    public void setdate(String date) {
        this.date = date;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    public String getOuttime() {
        return outtime;
    }

    public void setOuttime(String outtime) {
        this.outtime = outtime;
    }

    public String getPolicycount() {
        return policycount;
    }

    public void setPolicycount(String policycount) {
        this.policycount = policycount;
    }

    public String getLeadscount() {
        return leadscount;
    }

    public void setLeadscount(String leadscount) {
        this.leadscount = leadscount;
    }

    public String getOtwcount() {
        return otwcount;
    }

    public void setOtwcount(String otwcount) {
        this.otwcount = otwcount;
    }
}
