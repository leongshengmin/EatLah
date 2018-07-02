package com.eatlah.eatlah.adapters.Hawker;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.Hawker.CompletedOrderFragment;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.eatlah.eatlah.models.OrderItem} and makes a call to the
 * specified {@link CompletedOrderFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CompletedOrderRecyclerViewAdapter extends RecyclerView.Adapter<CompletedOrderRecyclerViewAdapter.ViewHolder> {
    private static int GREEN = 0xFF00FA9A;

    private final List<Order> mValues;
    private final CompletedOrderFragment.OnListFragmentInteractionListener mListener;

    public CompletedOrderRecyclerViewAdapter(List<Order> items, CompletedOrderFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hawker_view_holder_completedorder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Order order = mValues.get(position);
        holder.mName.setText(order.getTimestamp());
        holder.mDesc.setText("Completed");

        // Calculate price and set text
        String hawkerId = CompletedOrderFragment.user.get_hawkerId();
        calculatePrice(order, holder.mPrice, hawkerId);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(order, true);
            }
        });

        holder  .mCardView
                .setCardBackgroundColor(GREEN);
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
        private CardView mCardView;

        public ViewHolder(View view) {
            super(view);
            mName   = view.findViewById(R.id.completedorder_orderName_textView);
            mPrice  = view.findViewById(R.id.completedorder_price_textView);
            mDesc   = view.findViewById(R.id.completedorder_orderDesc_textView);
            mCardView = view.findViewById(R.id.completedorder_hc_view_holder);
        }
    }
}
