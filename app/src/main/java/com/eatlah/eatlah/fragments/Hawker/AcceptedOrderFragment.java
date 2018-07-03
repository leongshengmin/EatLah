package com.eatlah.eatlah.fragments.Hawker;

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
import com.eatlah.eatlah.adapters.Hawker.AcceptedOrderRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.eatlah.eatlah.models.User;
import com.google.firebase.auth.FirebaseAuth;
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
public class AcceptedOrderFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private FirebaseDatabase mDb;

    public static User user;

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private AcceptedOrderRecyclerViewAdapter mAdapter;
    List<Order> mOrderList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AcceptedOrderFragment() {}

    @SuppressWarnings("unused")
    public static AcceptedOrderFragment newInstance(int columnCount) {
        AcceptedOrderFragment fragment = new AcceptedOrderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mOrderList = new ArrayList<>();
        mDb = FirebaseDatabase.getInstance();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    /*
     * Retrieval of Orders from db for display in recyclerView body.
     * Only shows orders by the correct hawker
     */
    private void retrieveOrders() {
        DatabaseReference mDbRef = mDb.getReference("Orders");
        System.out.println("Retrieving orders.");
        final String hawkerId = user.get_hawkerId();
        mDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mOrderList.clear();
                // Look through all orders, add those with an order from this hawker.
                for (DataSnapshot fcSnapshot : dataSnapshot.getChildren()) {
                    Order o = fcSnapshot.getValue(Order.class);
                    // If one orderItem belongs to the user, add it.
                    boolean correctUser = false;
                    for (OrderItem oi : o.getOrders()) {
                        if (oi.getStall_id().equals(hawkerId)) {
                            correctUser = true;
                            break;
                        }
                    }
                    // Only show accepted orders that are not complete
                    if (correctUser && !o.isTransaction_complete()){
                        // if self collection, show.
                        if (o.isSelf_collection())
                            mOrderList.add(o);
                        // delivery - MUST HAVE COURIER ATTENDING, or don't show.
                        else if (o.isCourierAttending())
                            mOrderList.add(o);
                    }
                }
                notifyAdapter((Activity) mListener, mOrderList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
     * Notifies adapter to update recycler view due to change in dataset
     * @param context context to update view
     * @param orderList Orders list reference
     */
    private void notifyAdapter(Activity context, List<Order> orderList) {
        mOrderList = orderList;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hawker_fragment_acceptedorder_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new AcceptedOrderRecyclerViewAdapter(mOrderList, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Get user for the hawker ID
        String uid = FirebaseAuth.getInstance().getUid();
        mDb.getReference("users")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User currUser = dataSnapshot.getValue(User.class);
                        System.out.println("curr user: " + currUser.get_hawkerId());
                        AcceptedOrderFragment.user = currUser;
                        retrieveOrders();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onListFragmentInteraction(Order item);
    }

    public AcceptedOrderRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }
}
