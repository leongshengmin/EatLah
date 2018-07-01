package com.eatlah.eatlah.fragments.Customer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.Courier.CourierBasicOrderItemRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerReceiptFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomerReceiptFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerReceiptFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ORDER_TAG = "order";
    private static final String CUSTOMER_ADDRESS_TAG = "customerAddress";

    private Order order;
    private String customerAddress;

    private CourierBasicOrderItemRecyclerViewAdapter mAdapter;
    private CustomerReceiptFragment.OnFragmentInteractionListener mListener;

    private Button completedOrder_button;

    public CustomerReceiptFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param order
     * @param customerAddress
     * @return A new instance of fragment CourierReceiptFragment.
     */
    public static CustomerReceiptFragment newInstance(Order order, String customerAddress) {
        CustomerReceiptFragment fragment = new CustomerReceiptFragment();
        Bundle args = new Bundle();
        args.putSerializable(ORDER_TAG, order);
        args.putString(CUSTOMER_ADDRESS_TAG, customerAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.order = (Order) getArguments().getSerializable(ORDER_TAG);
            this.customerAddress = getArguments().getString(CUSTOMER_ADDRESS_TAG);
            System.out.println("On create fragment, order : " + order );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.courier_fragment_receipt, container, false);
        ((TextView) fragmentView.findViewById(R.id.customerId_textView)).setText("Customer ID: " + order.getUser_id());
        ((TextView) fragmentView.findViewById(R.id.customerAddress_textView)).setText("Customer Address: " + customerAddress);
        TextView subtotal_textView = fragmentView.findViewById(R.id.amtToCollect_textView);
        setSubtotal(subtotal_textView);
        completedOrder_button = fragmentView.findViewById(R.id.completedOrder_button);
        completedOrder_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrderCompletion();
            }
        });

        RecyclerView orderItemsRecyclerView = fragmentView.findViewById(R.id.orderItems_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((Activity) mListener);
        orderItemsRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CourierBasicOrderItemRecyclerViewAdapter((Activity)mListener, order.getOrders());
        orderItemsRecyclerView.setAdapter(mAdapter);
        return fragmentView;
    }

    private void setSubtotal(TextView subtotal_textView) {
        double cost = 0;
        for (OrderItem orderItem : order.getOrders()) {
            cost += Double.parseDouble(orderItem.getPrice());
        }
        subtotal_textView.setText(Double.toString(cost));
    }

    public void onOrderCompletion() {
        ((CustomerReceiptFragment.OnFragmentInteractionListener) mListener).onFragmentInteraction(order);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CustomerReceiptFragment.OnFragmentInteractionListener) {
            mListener = (CustomerReceiptFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Order order);
    }
}
