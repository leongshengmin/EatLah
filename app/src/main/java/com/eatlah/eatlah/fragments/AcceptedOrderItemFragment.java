package com.eatlah.eatlah.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.AcceptedOrderItemRecyclerViewAdapter;
import com.eatlah.eatlah.models.FoodItem;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AcceptedOrderItemFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<OrderItem> orderItemList;
    private AcceptedOrderItemRecyclerViewAdapter mAdapter;
    private static Order order; // Maybe this isn't necessary, will find out lol

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AcceptedOrderItemFragment() {
    }

    @SuppressWarnings("unused")
    public static AcceptedOrderItemFragment newInstance(int columnCount, Order _order) {
        order = _order;
        AcceptedOrderItemFragment fragment = new AcceptedOrderItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hawker_fragment_acceptedorderitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new AcceptedOrderItemRecyclerViewAdapter(orderItemList, mListener);

            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            orderItemList = new ArrayList<>();
            retrieveOrderItems();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void retrieveOrderItems() {
        FirebaseDatabase mDb = FirebaseDatabase.getInstance();
        DatabaseReference mDbRef = mDb.getReference(getResources().getString(R.string.food_item_ref));

        orderItemList.clear();

        for (final OrderItem orderItem : order.getOrders()) {

            // I don't know why when I remove this outer bracket down there V the thing breaks.
            mDbRef.child(orderItem.get_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FoodItem foodItem = dataSnapshot.getValue(FoodItem.class);
                    if (orderItem.getStall_id().equals(AcceptedOrderFragment.user.get_hawkerId())) {
                        orderItemList.add(orderItem);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static Order getOrder() {
        return order;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(OrderItem item);
    }
}
