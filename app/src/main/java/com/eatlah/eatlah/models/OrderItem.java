package com.eatlah.eatlah.models;

import java.io.Serializable;

public class OrderItem extends FoodItem implements Serializable {
    private int qty;

    public OrderItem(FoodItem foodItem, int qty) {
        super(foodItem.get_id(), foodItem.getStall_id(), foodItem.getName(), foodItem.getPrice(), foodItem.getImage_path(), foodItem.getDescription());
        this.qty = qty;
    }

    public OrderItem() {}

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof OrderItem && ((OrderItem) object).get_id().equals(this.get_id());
    }
}
