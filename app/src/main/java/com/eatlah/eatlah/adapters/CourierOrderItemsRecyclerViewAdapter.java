package com.eatlah.eatlah.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.CourierOrderItemsFragment.OnListFragmentInteractionListener;
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

    public CourierOrderItemsRecyclerViewAdapter(List<OrderItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.courier_orderitems_viewholder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final OrderItem orderItem = mValues.get(position);
        System.out.println("order item: " + orderItem.getName() + " " + orderItem.getQty());
        holder.mOrderName.setText(orderItem.getName());
        holder.mOrderQty.setText(Integer.toString(orderItem.getQty()));
        holder.mOrderDesc.setText(orderItem.getDescription());
        holder.mOrderStallId.setText(orderItem.getStall_id());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(orderItem);
                }
            }
        });
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

        public ViewHolder(View view) {
            super(view);
            mOrderName = view.findViewById(R.id.orderName_textView);
            mOrderQty = view.findViewById(R.id.orderQty_textView);
            mOrderDesc = view.findViewById(R.id.orderDesc_textView);
            mOrderStallId = view.findViewById(R.id.orderAddress_textView);
        }
    }
}
