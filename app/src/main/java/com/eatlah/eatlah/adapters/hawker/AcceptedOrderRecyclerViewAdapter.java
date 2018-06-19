package com.eatlah.eatlah.adapters.hawker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.hawker.AcceptedOrderFragment;
import com.eatlah.eatlah.fragments.hawker.AcceptedOrderFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AcceptedOrderRecyclerViewAdapter extends RecyclerView.Adapter<AcceptedOrderRecyclerViewAdapter.ViewHolder> {

    private final List<Order> mValues;
    private final OnListFragmentInteractionListener mListener;

    public AcceptedOrderRecyclerViewAdapter(List<Order> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hawker_view_holder_acceptedorder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Order order = mValues.get(position);
        holder.mName.setText(order.getCollectionTime());
        holder.mDesc.setText(order.isReady() ? "Ready!" : "Not ready.");

        // Calculate price and set text
        String hawkerId = AcceptedOrderFragment.user.get_hawkerId();
        calculatePrice(order, holder.mPrice, hawkerId);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(order);
            }
        });
    }

    private void calculatePrice(Order order, TextView priceView, String hawkerId) {
        double price = 0;
        for (OrderItem orderItem : order.getOrders()) {
            if (orderItem.getStall_id().equals(hawkerId))
                price += orderItem.getQty() * Double.parseDouble(orderItem.getPrice());
        }
        priceView.setText(Double.toString(price));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mName;
        private TextView mPrice;
        private TextView mDesc;

        public ViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.acceptedorder_orderName_textView);
            mPrice = view.findViewById(R.id.acceptedorder_price_textView);
            mDesc = view.findViewById(R.id.acceptedorder_orderDesc_textView);
        }
    }
}
