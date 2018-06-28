package com.eatlah.eatlah.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.Courier.CourierPendingOrderFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.models.Order;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Order} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CourierPendingOrderRecyclerViewAdapter extends RecyclerView.Adapter<CourierPendingOrderRecyclerViewAdapter.ViewHolder> {

    private final List<Order> mValues;
    private final OnListFragmentInteractionListener mListener;

    public CourierPendingOrderRecyclerViewAdapter(List<Order> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.courier_view_holder_pending_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Order order = mValues.get(position);

        holder.mTimeStampView.setText(order.getCollectionTime());
        holder.mAddressView.setText(order.getHawkerCentre_id());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(order);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTimeStampView;
        private TextView mAddressView;

        public ViewHolder(View view) {
            super(view);
            mTimeStampView = view.findViewById(R.id.orderName_textView);
            mAddressView = view.findViewById(R.id.orderAddress_textView);
        }
    }
}
