package com.eatlah.eatlah.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Order {
    private String timestamp;
    private String user_id; // mobile number of customer
    private String courier_id;  // mobile number of courier
    private String hawkerCentre_id; // postal code of hc
    private List<OrderItem> orders;
    private String misc;
    private boolean self_collection;
    private boolean ready;

    public Order(String timestamp, String user_id,
                 String courier_id, String hawkerCentre_id, List<OrderItem> orders,
                 String misc, boolean self_collection, boolean ready) {
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.courier_id = courier_id;
        this.hawkerCentre_id = hawkerCentre_id;
        this.orders = orders;
        this.misc = misc;
        this.self_collection = self_collection;
        this.ready = ready;
    }

    public Order(String timestamp, String user_id, String hawkerCentre_id) {
        this(timestamp, user_id, null, hawkerCentre_id, new ArrayList<OrderItem>(),
                null, true, false);
    }

    public Order() {}

    /**
     * adds an order to orders.
     * @param item
     */
    public void addOrder(OrderItem item) {
        if (!orders.contains(item)) {
            orders.add(item);
        } else {
            orders.remove(item);
            orders.add(item);
        }
    }

    /**
     * courier has chosen to attend to this order.
     * @param courier_id
     */
    public void setCourier_id(String courier_id) {
        this.courier_id = courier_id;
        self_collection = false;
    }

    /**
     * hawker is done with order and order is available for pickup.
     */
    public void markReady() {
        ready = true;
        //todo notify courier
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public String getCourier_id() {
        return courier_id;
    }

    public String getHawkerCentre_id() {
        return hawkerCentre_id;
    }

    public String getMisc() {
        return misc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setHawkerCentre_id(String hawkerCentre_id) {
        this.hawkerCentre_id = hawkerCentre_id;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setSelf_collection(boolean self_collection) {
        this.self_collection = self_collection;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
