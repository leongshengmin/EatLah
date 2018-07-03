package com.eatlah.eatlah.models;

public class User {
    private String _id;
    private String email;
    private String address;
    private String _hawkerId;
    private String _hawkerCentreId;

    public User(String _id, String email, String hawkerId, String hawkerCentreId, String address) {
        this._id = _id;
        this.email = email;
        _hawkerId = hawkerId;
        _hawkerCentreId = hawkerCentreId;
        this.address = address;
    }

    public String get_hawkerCentreId() { return _hawkerCentreId; }

    public void set_hawkerCentreId(String _hawkerCentreId) { this._hawkerCentreId = _hawkerCentreId; }

    public User() {}

    public String get_id() {
        return _id;
    }

    public String getAddress() {
        return address;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public String get_hawkerId() { return _hawkerId; }

    public void set_hawkerId(String _hawkerId) { this._hawkerId = _hawkerId; }
}
