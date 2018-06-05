package com.eatlah.eatlah.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.CustomerHomepage;
import com.eatlah.eatlah.fragments.OrderFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.listeners.OnSwipeTouchListener;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderRecyclerViewAdapter extends RecyclerView.Adapter<OrderRecyclerViewAdapter.ViewHolder> {

    private final List<OrderItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public OrderRecyclerViewAdapter(List<OrderItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_view_holder_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final OrderItem orderItem = mValues.get(position);
        holder.mName.setText(orderItem.getName());
        holder.mDesc.setText(orderItem.getDescription());

        // calculate price and set text
        calculatePrice(orderItem, holder.mPrice);

        holder.mQty.setText(Integer.toString(orderItem.getQty()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(orderItem);
            }
        });

        // left swipe decrements the quantity of order item by 1
        // right swipe increments qty of order item by 1
        holder.itemView.setOnTouchListener(new OnSwipeTouchListener((Activity) mListener, this, orderItem));


        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CustomerHomepage) mListener).getOrder()
                        .removeAll(orderItem, OrderRecyclerViewAdapter.this);
            }
        });
    }

    private void calculatePrice(OrderItem orderItem, TextView priceView) {
        System.out.println("ordered item: " + orderItem);
        double price = orderItem.getQty() * Double.parseDouble(orderItem.getPrice());
        priceView.setText(Double.toString(price));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mName;
        private TextView mQty;
        private TextView mPrice;
        private TextView mDesc;
        private Button mCancelButton;

        public ViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.orderName_textView);
            mQty = (TextView) view.findViewById(R.id.orderQty_textView);
            mPrice = view.findViewById(R.id.price_textView);
            mDesc = view.findViewById(R.id.orderDesc_textView);
            mCancelButton = view.findViewById(R.id.cancel_button);
        }

    }
}
