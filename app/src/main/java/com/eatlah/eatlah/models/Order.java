package com.eatlah.eatlah.models;

import com.eatlah.eatlah.adapters.Customer.OrderRecyclerViewAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order implements Serializable {
    private String timestamp;
    private String user_id; // mauth uid of customer
    private String courier_id;  // mauth uid of courier
    private String hawkerCentre_id; // postal code of hc
    private HashMap<String, Integer> orderDict;  // stores the orderitem as key and index in orders as value
    private ArrayList<OrderItem> orders;
    private String misc;
    private String collectionTime;
    private boolean self_collection;
    private boolean ready;  // is the order ready for collection
    private boolean transaction_complete;

    public Order(String timestamp, String user_id,
                 String courier_id, String hawkerCentre_id,
                 ArrayList<OrderItem> orders, String misc, String collectionTime,
                 boolean self_collection, boolean ready, boolean transaction_complete) {
        this.orderDict = new HashMap<>();
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.courier_id = courier_id;
        this.hawkerCentre_id = hawkerCentre_id;
        this.orders = orders;
        this.misc = misc;
        this.collectionTime = collectionTime;
        this.self_collection = self_collection;
        this.ready = ready;
        this.transaction_complete = transaction_complete;
    }

    public Order(String timestamp, String user_id, String hawkerCentre_id) {
        this(timestamp, user_id, null, hawkerCentre_id, new ArrayList<OrderItem>(),
                null, null, true, false, false);
    }

    public Order() {}

    /**
     * adds an order to orders.
     * @param item
     */
    public void addOrder(OrderItem item, OrderRecyclerViewAdapter adapter) {
        if (!orderDict.containsKey(item.get_id())) {
            System.out.println("adding new item: " + item.getName());
            orderDict.put(item.get_id(), orders.size());
            orders.add(item);
        } else {
            System.out.println("item already added, updating quantity of " + item.getName());
            int idx = orderDict.get(item.get_id());
            OrderItem prev = orders.get(idx);   // get the previous order
            item.setQty(prev.getQty() + item.getQty()); // update quantity
            orders.set(idx, item);

            // notify adapter
            adapter.notifyItemChanged(idx);
        }
        System.out.println(orders.size() + " orders currently");
    }

    /**
     * overloaded method which adds item to order without notifying adapter.
     * @param item
     */
    public void addOrder(OrderItem item) {
        if (!orderDict.containsKey(item.get_id())) {
            System.out.println("adding new item: " + item.getName());
            orderDict.put(item.get_id(), orders.size());
            orders.add(item);
        } else {
            System.out.println("item already added, updating quantity of " + item.getName());
            int idx = orderDict.get(item.get_id());
            OrderItem prev = orders.get(idx);   // get the previous order
            item.setQty(prev.getQty() + item.getQty()); // update quantity
            orders.set(idx, item);
        }
        System.out.println(orders.size() + " orders currently");
    }

    /**
     * removes an order from orders.
     * @param orderItem
     */

    public void removeOrder(OrderItem orderItem, OrderRecyclerViewAdapter adapter) {
        int idx = orderDict.get(orderItem.get_id());

        if (orderItem.getQty() == 1) {
            // swiping will remove this item from view
            System.out.println("removing this item from orders " + orderItem.getName());
            shiftItemsOnRight(idx);
            adapter.notifyItemRemoved(idx);
        } else if (orderItem.getQty() > 1) {    // update quantity
            System.out.println("updating quantity of this item " + orderItem.getName());
            orderItem.setQty(orderItem.getQty() - 1);
            orders.set(idx, orderItem);

            // notify adapter
            adapter.notifyItemChanged(idx);
        }
    }

    /**
     * removes the order item entirely from cart.
     * @param orderItem
     * @param adapter
     */
    public void removeAll(OrderItem orderItem, OrderRecyclerViewAdapter adapter) {
        int idx = orderDict.get(orderItem.get_id());
        orderDict.remove(orderItem.get_id());
        System.out.println("removing this item from orders " + orderItem.getName());
        shiftItemsOnRight(idx);
        // notify adapter
        adapter.notifyItemRangeChanged(idx, orders.size() - idx);
    }

    /**
     * shifts items to the right of orderItem with index idx to the left
     * then removes the last item in the list.
     */
    private void shiftItemsOnRight(int idx) {
        orders.remove(idx);
        for (Map.Entry<String, Integer> entry : orderDict.entrySet()) {
            if (entry.getValue() == idx) orderDict.remove(entry.getKey());
            if (entry.getValue() > idx) {
                orderDict.put(entry.getKey(), entry.getValue() - 1);
            }
        }
    }

    public boolean hasOrders() {
        return !orderDict.isEmpty();
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

    public boolean isTransaction_complete() { return transaction_complete; }

    public void setTransaction_complete(boolean transaction_complete) { this.transaction_complete = transaction_complete; }

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

    public boolean isReady() {
        return ready;
    }

    public boolean isSelf_collection() {
        return self_collection;
    }

    public String getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(String collectionTime) {
        this.collectionTime = collectionTime;
    }

    public void setHawkerCentre_id(String hawkerCentre_id) {
        this.hawkerCentre_id = hawkerCentre_id;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }

    public void setOrders(ArrayList<OrderItem> orders) {
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

    /**
     * checks if a courier is already attending to this order
     * @return true if a courier is already attending to this order, false otherwise.
     */
    public boolean isCourierAttending() {
        return !(courier_id == null || courier_id.isEmpty());
    }
    // Fake setter to compile properly
    public void setCourierAttending(boolean b) {}
}
