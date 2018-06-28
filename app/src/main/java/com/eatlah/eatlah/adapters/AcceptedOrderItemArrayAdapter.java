package com.eatlah.eatlah.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

public class AcceptedOrderItemArrayAdapter extends ArrayAdapter<OrderItem> {
    private Activity context;
    private List<OrderItem> orderItemList;


    public AcceptedOrderItemArrayAdapter(@NonNull Context context, int resource, @NonNull List<OrderItem> objects) {
        super(context, resource, objects);
    }
}
