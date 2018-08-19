package com.eatlah.eatlah.adapters.Courier;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.Courier.CourierHomepage;
import com.eatlah.eatlah.models.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                .inflate(R.layout.general_view_holder_past_orders, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.item = mValues.get(position);
        holder.orderIdView.setText(mValues.get(position).getTimestamp());
        holder.customerIdView.setText(mValues.get(position).getUser_id());
        holder.timeView.setText(mValues.get(position).getCollectionTime());
        holder.orderItemsView.setLayoutManager(new LinearLayoutManager((Activity) mListener));
        holder.orderItemsView.setAdapter(new CourierBasicOrderItemRecyclerViewAdapter((Activity)mListener, holder.item.getOrders()));

        // Address into textview and receipt
        String userId = mValues.get(position).getUser_id();
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("address")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String address = (String) dataSnapshot.getValue();
                        holder.orderAdress_textView.setText(address);
                        System.out.println("address set @@@@@@@@@@@@@@@@@@@@@");

                        holder.cardView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.out.println("onclick set @@@@@@@@@@@@@@@@@@@@@");
                                ((CourierHomepage) mListener).displayCourierReceipt(mValues.get(position),address);

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });


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
        TextView orderAdress_textView;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardView);
            orderAdress_textView = view.findViewById(R.id.orderAdress_textView);
            orderIdView = view.findViewById(R.id.orderId_textView);
            customerIdView = view.findViewById(R.id.customerId_textView);
            timeView = view.findViewById(R.id.orderTime_textView);
            orderItemsView = view.findViewById(R.id.orderItem_recyclerView);
        }
    }
}
