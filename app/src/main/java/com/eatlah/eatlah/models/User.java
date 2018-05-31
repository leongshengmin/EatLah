package com.eatlah.eatlah.models;

public class User {
    private String _id;
    private String email;

    public User(String _id, String email) {
        this._id = _id;
        this.email = email;
    }

    public User() {}

    public String get_id() {
        return _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
