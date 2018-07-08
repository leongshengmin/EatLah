package com.eatlah.eatlah.fragments.Courier;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.Courier.CourierOrderItemsRecyclerViewAdapter;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

import java.util.List;

public class CourierOrderItemsDialogFragment extends DialogFragment {

    private CourierOrderItemsFragment.OnListFragmentInteractionListener context;
    private List<OrderItem> orderItemList;
    private CourierOrderItemsRecyclerViewAdapter mAdapter;
    private Order mOrder;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOrder = (Order) getArguments()
                .getSerializable(getResources().getString(R.string.order_ref));
        orderItemList = mOrder.getOrders();
        context = (CourierOrderItemsFragment.OnListFragmentInteractionListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.general_fragment_orderitems_list, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.list);
        mAdapter = new CourierOrderItemsRecyclerViewAdapter(orderItemList, context);
        recyclerView.setAdapter(mAdapter);

        Log.d("map", "courier map dialog view created.");

        Button attendToOrder_btn = getActivity().findViewById(R.id.attendToOrder_button);
        if (attendToOrder_btn.getVisibility() == View.INVISIBLE) {
            mAdapter.hasAttendedToOrder = true;
            Log.d("map", "attend to order button is invisible. Updating adapter...");
        } else {
            mAdapter.hasAttendedToOrder = false;
            Log.d("map", "attend to order button is visible. Updating adapter...");
        }

        return v;
    }

}
