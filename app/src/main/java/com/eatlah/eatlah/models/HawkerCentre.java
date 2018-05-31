package com.eatlah.eatlah.models;

public class HawkerCentre {

    private String _id;
    private String name;
    private String image_path;

    HawkerCentre(String _id, String name, String image_path) {
        this._id = _id;
        this.name = name;
        this.image_path = image_path;
    }

    HawkerCentre() {}

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getImage_path() {
        return image_path;
    }
}
