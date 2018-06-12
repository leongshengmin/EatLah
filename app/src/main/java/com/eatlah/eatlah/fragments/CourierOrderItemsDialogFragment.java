package com.eatlah.eatlah.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.CourierOrderItemsRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

public class CourierOrderItemsDialogFragment extends DialogFragment {

    private CourierOrderItemsFragment.OnListFragmentInteractionListener context;
    private List<OrderItem> orderItemList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Order order = (Order) getArguments()
                .getSerializable(getResources().getString(R.string.order_ref));
        orderItemList = order.getOrders();
        context = (CourierOrderItemsFragment.OnListFragmentInteractionListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.courier_fragment_orderitems_list, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.list);
        recyclerView.setAdapter(new CourierOrderItemsRecyclerViewAdapter(orderItemList, context));

        return v;
    }
}
