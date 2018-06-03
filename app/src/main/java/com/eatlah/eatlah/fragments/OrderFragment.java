package com.eatlah.eatlah.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.MyOrderRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class OrderFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MyOrderRecyclerViewAdapter mAdapter;
    private FloatingActionButton submit_btn;
    private static Order mOrder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrderFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OrderFragment newInstance(int columnCount, Order order) {
        mOrder = order;
        OrderFragment fragment = new OrderFragment();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void displayButton() {
        // hides fab
        FloatingActionButton fab = ((Activity)mListener).findViewById(R.id.fab);
        fab.hide();

        // displays the button to submit or cancel order
        submit_btn = ((Activity)mListener).findViewById(R.id.submit_or_cancel_button);
        submit_btn.setTooltipText(getButtonDisplayText());
        submit_btn.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            displayButton();

            mAdapter = new MyOrderRecyclerViewAdapter(mOrder.getOrders(), mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    /**
     * returns a string corresponding to text displayed on orderView button
     * @return
     */
    private String getButtonDisplayText() {
        if (!alertCartEmpty()) {    // cart is not empty
            submit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText((Activity) mListener, "Submitted order!", Toast.LENGTH_SHORT).show();
                }
            });
            return ((Activity)mListener).getResources().getString(R.string.order_submit_button);
        }

        // cart is empty
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOrder();
            }
        });
        return ((Activity)mListener).getResources().getString(R.string.order_cancel_button);
    }

    /**
     * cancels customer's order
     * empties cart and order session.
     */
    private void deleteOrder() {
        FirebaseDatabase
                .getInstance()
                .getReference(((Activity)mListener).getResources().getString(R.string.order_ref))
                .child(mOrder.getTimestamp())
                .setValue(null)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText((Activity)mListener, "Cancelled order.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("order", task.getException().getMessage());
                        }
                    }
                });
    }

    private boolean alertCartEmpty() {
        if (!mOrder.hasOrders()) {
            Toast.makeText((Activity) mListener, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
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

    public MyOrderRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }
}
