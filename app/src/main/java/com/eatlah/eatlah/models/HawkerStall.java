package com.eatlah.eatlah.models;

import java.util.List;

public class HawkerStall {

    private String hc_id;
    private String _id;
    private String stall_name;
    private String stall_owner;
    private List<String> menu;
    private String image_path;

    public HawkerStall(String hc_id, String _id, String stall_name, String stall_owner, List<String> menu, String image_path) {
        this._id = _id;
        this.hc_id = hc_id;
        this.stall_name = stall_name;
        this.stall_owner = stall_owner;
        this.menu = menu;
        this.image_path = image_path;
    }

    public HawkerStall() {}

    public String getHc_id() {
        return hc_id;
    }

    public String getImage_path() {
        return image_path;
    }

    public String getStall_owner() {
        return stall_owner;
    }

    public String getStall_name() {
        return stall_name;
    }

    public String get_id() {
        return _id;
    }

    public List<String> getMenu() {
        return menu;
    }

    public void setName(String stall_name) {
        this.stall_name = stall_name;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setMenu(List<String> menu) {
        this.menu = menu;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public void setHc_id(String hc_id) {
        this.hc_id = hc_id;
    }

    public void setStall_owner(String stall_owner) {
        this.stall_owner = stall_owner;
    }
}
