package com.eatlah.eatlah.fragments.Courier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.Courier.CourierHomepage;
import com.eatlah.eatlah.adapters.Courier.CourierOrderItemsRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.eatlah.eatlah.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CourierOrderItemsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;

    private CourierOrderItemsRecyclerViewAdapter mAdapter;
    private OnListFragmentInteractionListener mListener;
    private Order mOrder;
    private String customerAddress;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CourierOrderItemsFragment() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
    }

    @SuppressWarnings("unused")
    public static CourierOrderItemsFragment newInstance(int columnCount) {
        CourierOrderItemsFragment fragment = new CourierOrderItemsFragment();
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

            // retrieve the Order obj containing list of ordered foodItems
            mOrder = (Order) getArguments()
                    .getSerializable(((Activity)mListener).getResources().getString(R.string.order_ref));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_fragment_orderitems_list, container, false);
        initAttendToOrderButton(view);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new CourierOrderItemsRecyclerViewAdapter(mOrder.getOrders(), mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    private void initAttendToOrderButton(final View view) {
        final Button attendToOrderBtn = ((Activity) mListener).findViewById(R.id.attendToOrderButton);
        attendToOrderBtn.setVisibility(View.VISIBLE);
        attendToOrderBtn.setText(((Activity) mListener).getResources().getString(R.string.attendToOrder));
        attendToOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveCustomerAddress();
                // update order fields in db
                // to remove order from global courier view
                updateOrder();

                initCompletedOrderButton();
            }

            private void retrieveCustomerAddress() {
                System.out.println(mOrder.getUser_id());
                mDb.getReference(getResources().getString(R.string.user_ref))
                        .child(mOrder.getUser_id())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                System.out.println("datasnapshot: " + dataSnapshot);
                                User customer = dataSnapshot.getValue(User.class);
                                customerAddress = customer.getAddress();
                                System.out.println("customer address retrieved: " + customerAddress);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("db", databaseError.getMessage());
                            }
                        });
            }

            private void initCompletedOrderButton() {
                if (customerAddress == null) return;
                attendToOrderBtn.setVisibility(View.INVISIBLE);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(CourierOrderItemsFragment.this);
                ft.commit();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ((CourierHomepage)mListener).displayCourierReceipt(mOrder, customerAddress);
    }

    /**
     * updates the order in db
     * since CourierPendingOrderFrag is listening to real-time value changes to orders in db
     * UI will be updated automatically.
     */
    private void updateOrder() {
        mOrder.setCourier_id(mAuth.getUid());
        mDb.getReference(((Activity)mListener).getResources().getString(R.string.order_ref))
                .child(mOrder.getTimestamp())
                .setValue(mOrder);
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
        void onListFragmentInteraction(OrderItem item);
    }
}
