package com.eatlah.eatlah.adapters;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.Courier.CourierHomepage;
import com.eatlah.eatlah.activities.Customer.CustomerHomepage;
import com.eatlah.eatlah.models.Order;

import java.util.List;

public class PastOrdersRecyclerViewAdapter extends RecyclerView.Adapter<PastOrdersRecyclerViewAdapter.ViewHolder> {

    private final List<Order> mValues;
    private final Activity mListener;

    public PastOrdersRecyclerViewAdapter(List<Order> items, Activity listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.past_orders_viewholder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = mValues.get(position);
        holder.orderIdView.setText(mValues.get(position).getTimestamp());
        bindID(holder);
        holder.timeView.setText(mValues.get(position).getCollectionTime());
        holder.orderItemsView.setLayoutManager(new LinearLayoutManager((Activity) mListener));
        holder.orderItemsView.setAdapter(new CourierBasicOrderItemRecyclerViewAdapter((Activity)mListener, holder.item.getOrders()));
    }

    private void bindID(ViewHolder holder) {
        if (mListener instanceof CourierHomepage) {
            holder.customerIdView.setText(holder.item.getUser_id());
        } else if (mListener instanceof CustomerHomepage) {
            holder.customerIdView.setText(holder.item.getCourier_id());
        }
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
