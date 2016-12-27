package com.invicibledevs.elixir.model;

import java.util.ArrayList;

/**
 * Created by nandhakumarv on 16/04/16.
 */
public class Role {

    private String roleId;
    private String roleName;
    private ArrayList<String> collectorsName;
    private ArrayList<String> collectorsId;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public ArrayList<String> getCollectorsName() {
        return collectorsName;
    }

    public void setCollectorsName(ArrayList<String> collectorsName) {
        this.collectorsName = collectorsName;
    }

    public ArrayList<String> getCollectorsId() {
        return collectorsId;
    }

    public void setCollectorsId(ArrayList<String> collectorsId) {
        this.collectorsId = collectorsId;
    }
}
