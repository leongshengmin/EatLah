package com.eatlah.eatlah.adapters.Courier;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.Courier.CourierOrderItemsFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CourierOrderItemsRecyclerViewAdapter extends RecyclerView.Adapter<CourierOrderItemsRecyclerViewAdapter.ViewHolder> {

    private final List<OrderItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private int ordersCollected;
    public boolean hasAttendedToOrder;

    public CourierOrderItemsRecyclerViewAdapter(List<OrderItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        ordersCollected = 0;
        hasAttendedToOrder = true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.general_view_holder_orderitems, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final OrderItem orderItem = mValues.get(position);
        System.out.println("order item: " + orderItem.getName() + " " + orderItem.getQty());
        holder.mOrderName.setText(orderItem.getName());
        holder.mOrderQty.setText("Quantity: " + Integer.toString(orderItem.getQty()));
        holder.mOrderDesc.setText(orderItem.getDescription());
        holder.mOrderStallId.setText("Stall ID: " + orderItem.getStall_id());
        toggleCheckboxVisibility(holder.mCollected_checkbox);

        holder.mCollected_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.collectedOrder_checkbox && ((CheckBox) v).isChecked()) {
                    recordCollectedOrderItem(orderItem);
                }
            }
        });

    }

    public void toggleCheckboxVisibility(CheckBox mCollected_checkbox) {
        if(hasAttendedToOrder) {
            mCollected_checkbox.setVisibility(View.VISIBLE);
        } else {
            mCollected_checkbox.setVisibility(View.INVISIBLE);
        }
    }


    private void recordCollectedOrderItem(OrderItem orderItem) {
        ordersCollected++;
        if (ordersCollected >= mValues.size()) {    // collected all orders
            System.out.println("Collected all orders");
            mListener.onListFragmentInteraction(orderItem);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mOrderName;
        private TextView mOrderQty;
        private TextView mOrderDesc;
        private TextView mOrderStallId;
        private CheckBox mCollected_checkbox;

        public ViewHolder(View view) {
            super(view);
            mCollected_checkbox = view.findViewById(R.id.collectedOrder_checkbox);
            mCollected_checkbox.setVisibility(View.INVISIBLE);
            mCollected_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mCollected_checkbox.setChecked(true);
                    }
                }
            });
            mOrderName = view.findViewById(R.id.orderName_textView);
            mOrderQty = view.findViewById(R.id.orderQty_textView);
            mOrderDesc = view.findViewById(R.id.orderDesc_textView);
            mOrderStallId = view.findViewById(R.id.orderAddress_textView);
        }
    }
}
