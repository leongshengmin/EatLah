package com.eatlah.eatlah.models;

public class User {
    private String _id;
    private String email;
    private String _hawkerId;


    public User(String _id, String email, String hawkerId) {
        this._id = _id;
        this.email = email;
        _hawkerId = hawkerId;

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

    public String get_hawkerId() { return _hawkerId; }

    public void set_hawkerId(String _hawkerId) { this._hawkerId = _hawkerId; }
}
