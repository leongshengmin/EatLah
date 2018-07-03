package com.eatlah.eatlah;

import com.eatlah.eatlah.models.Order;

import java.io.Serializable;

public class Cart implements Serializable {
    Order order;

    public boolean hasOrder() {
        return order != null;
    }

    public void makeStartingOrder(Order _order) {
        order = _order;
    }

    public Order getContents() {
        return this.order;
    }
}
