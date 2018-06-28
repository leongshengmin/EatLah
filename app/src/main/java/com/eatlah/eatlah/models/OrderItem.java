package com.eatlah.eatlah.models;

import java.io.Serializable;

public class OrderItem extends FoodItem implements Serializable {
    private int qty;
    private boolean complete;



    public OrderItem(FoodItem foodItem, int qty) {
        super(foodItem.get_id(), foodItem.getStall_id(), foodItem.getName(), foodItem.getPrice(), foodItem.getImage_path(), foodItem.getDescription());
        this.qty = qty;
        complete = false;

    }

    public OrderItem() {}

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public boolean isComplete() { return complete; }

    public void setComplete(boolean complete) { this.complete = complete; }
    @Override
    public boolean equals(Object object) {
        return object instanceof OrderItem && ((OrderItem) object).get_id().equals(this.get_id());
    }
}
