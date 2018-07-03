package com.eatlah.eatlah.fragments.General;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.Customer.CustomerHomepage;
import com.eatlah.eatlah.adapters.General.PastOrdersRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class PastOrdersFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String PAST_ORDERS_LIST = "pastOrdersList";
    private static final String ACC_TYPE = "acc_type";

    public final static String COURIER = "COURIER";
    public final static String CUSTOMER = "CUSTOMER";

    private int mColumnCount = 1;
    private List<Order> mPastOrders;
    private String mAccType;

    private Activity mListener;
    private PastOrdersRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PastOrdersFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PastOrdersFragment newInstance(int columnCount, ArrayList<Order> pastOrders, String accType) {
        PastOrdersFragment fragment = new PastOrdersFragment();
        Bundle args = new Bundle();
        args.putSerializable(PAST_ORDERS_LIST, pastOrders);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ACC_TYPE, accType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPastOrders = (ArrayList<Order>) getArguments().getSerializable(PAST_ORDERS_LIST);
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mAccType = getArguments().getString(ACC_TYPE);
        }

        // Retrieve orders (USER)
        if (mAccType.equals(CUSTOMER)) {
            FirebaseDatabase
                    .getInstance()
                    .getReference("Orders")
                    .orderByChild("user_id")
                    .equalTo(((CustomerHomepage) getActivity()).getUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mPastOrders.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                System.out.println("snap: " + snapshot);
                                Order order = snapshot.getValue(Order.class);
                                mPastOrders.add(order);
                            }
                            if (mListener == null) mListener = getActivity();
                            if (mListener != null) {
                                mListener.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        // Retrieve COURIER orders
        } else {
            FirebaseDatabase
                    .getInstance()
                    .getReference("Orders")
                    .orderByChild("courier_id")
                    .equalTo(FirebaseAuth.getInstance().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mPastOrders.clear();
                            System.out.println("datasnapshot contains: " + dataSnapshot);
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                System.out.println("snap: " + snapshot);
                                Order order = snapshot.getValue(Order.class);
                                mPastOrders.add(order);
                            }
                            if (mListener == null) mListener = getActivity();
                            if (mListener != null) {
                                mListener.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_fragment_past_orders_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new PastOrdersRecyclerViewAdapter(mPastOrders, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
