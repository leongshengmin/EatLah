package com.eatlah.eatlah.adapters.Courier;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

public class CourierBasicOrderItemRecyclerViewAdapter extends RecyclerView.Adapter<CourierBasicOrderItemRecyclerViewAdapter.ViewHolder> {
    private List<OrderItem> mOrderItems;
    private Activity mContext;

    public CourierBasicOrderItemRecyclerViewAdapter(Activity context, List<OrderItem> orderItems) {
        mContext = context;
        mOrderItems = orderItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mContext.getLayoutInflater().inflate(R.layout.courier_orderitems_viewholder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = mOrderItems.get(position);
        holder.mOrderName.setText(orderItem.getName());
        holder.mOrderQty.setText(Integer.toString(orderItem.getQty()));
        holder.mOrderStallId.setText(orderItem.getStall_id());
    }

    @Override
    public int getItemCount() {
        return mOrderItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mOrderName;
        TextView mOrderQty;
        TextView mOrderStallId;
        CheckBox mCollected_checkbox;

        public ViewHolder(View view) {
            super(view);
            mCollected_checkbox = view.findViewById(R.id.collectedOrder_checkbox);
            mCollected_checkbox.setVisibility(View.INVISIBLE);
            mOrderName = view.findViewById(R.id.orderName_textView);
            mOrderQty = view.findViewById(R.id.orderQty_textView);
            mOrderStallId = view.findViewById(R.id.orderAddress_textView);
        }
    }
}
