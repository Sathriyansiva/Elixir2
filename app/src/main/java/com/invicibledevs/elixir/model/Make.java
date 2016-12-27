package com.invicibledevs.elixir.model;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 27/07/16.
 */
public class Make {

    private String makeID;
    private String makeName;
    private ArrayList<Model> models;

    public String getMakeID() {
        return makeID;
    }

    public void setMakeID(String makeID) {
        this.makeID = makeID;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public ArrayList<Model> getModels() {
        return models;
    }

    public void setModels(ArrayList<Model> models) {
        this.models = models;
    }
}
