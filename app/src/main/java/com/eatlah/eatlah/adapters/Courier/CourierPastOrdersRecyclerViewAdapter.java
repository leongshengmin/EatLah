package com.eatlah.eatlah.adapters.Courier;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.Order;

import java.util.List;

public class CourierPastOrdersRecyclerViewAdapter extends RecyclerView.Adapter<CourierPastOrdersRecyclerViewAdapter.ViewHolder> {

    private final List<Order> mValues;
    private final Activity mListener;

    public CourierPastOrdersRecyclerViewAdapter(List<Order> items, Activity listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.courier_past_orders_viewholder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = mValues.get(position);
        holder.orderIdView.setText(mValues.get(position).getTimestamp());
        holder.customerIdView.setText(mValues.get(position).getUser_id());
        holder.timeView.setText(mValues.get(position).getCollectionTime());
        holder.orderItemsView.setLayoutManager(new LinearLayoutManager((Activity) mListener));
        holder.orderItemsView.setAdapter(new CourierBasicOrderItemRecyclerViewAdapter((Activity)mListener, holder.item.getOrders()));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdView;
        TextView customerIdView;
        TextView timeView;
        RecyclerView orderItemsView;
        Order item;

        public ViewHolder(View view) {
            super(view);
            orderIdView = view.findViewById(R.id.orderId_textView);
            customerIdView = view.findViewById(R.id.customerId_textView);
            timeView = view.findViewById(R.id.orderTime_textView);
            orderItemsView = view.findViewById(R.id.orderItem_recyclerView);
        }
    }
}
