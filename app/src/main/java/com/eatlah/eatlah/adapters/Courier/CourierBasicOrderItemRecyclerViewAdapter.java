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

import java.text.DecimalFormat;
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
        View view = mContext.getLayoutInflater().inflate(R.layout.general_view_holder_orderitems, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = mOrderItems.get(position);
        holder.mOrderName.setText(orderItem.getName());
        DecimalFormat df = new DecimalFormat("##.00");
        holder.mOrderQty.setText(String.format("Quantity: %d", orderItem.getQty()));
        holder.mOrderStallId.setText("Stall " + orderItem.getStall_id());
        holder.mOrderDesc.setText("Status: " + (orderItem.isComplete() ? "Ready" : "Not ready"));
        holder.mOrderPrice.setText("Price: $" +
                df.format(Float.parseFloat(orderItem.getPrice()) * (float) orderItem.getQty()));
    }

    @Override
    public int getItemCount() {
        return mOrderItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mOrderName;
        TextView mOrderQty;
        TextView mOrderStallId;
        TextView mOrderDesc;
        CheckBox mCollected_checkbox;
        TextView mOrderPrice;

        public ViewHolder(View view) {
            super(view);
            mCollected_checkbox = view.findViewById(R.id.collectedOrder_checkbox);
            mCollected_checkbox.setVisibility(View.INVISIBLE);
            mOrderName = view.findViewById(R.id.orderName_textView);
            mOrderQty = view.findViewById(R.id.orderQty_textView);
            mOrderStallId = view.findViewById(R.id.orderAddress_textView);
            mOrderDesc = view.findViewById(R.id.orderDesc_textView);
            mOrderPrice = view.findViewById(R.id.orderPrice_textView);
        }
    }
}
