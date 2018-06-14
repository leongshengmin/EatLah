package com.eatlah.eatlah.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eatlah.eatlah.helpers.MyFirebaseMessagingService;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.MessageRecyclerViewAdapter;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class MessageFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_MESSAGES = "messages";

    private int mColumnCount = 1;
    private List<String> messages;
    private Activity mContext;
    private MessageRecyclerViewAdapter mAdapter;

    private final FirebaseMessaging mFirebaseMessaging;
    private final MyFirebaseMessagingService messagingService;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageFragment() {
        messagingService = new MyFirebaseMessagingService();
        mFirebaseMessaging = FirebaseMessaging.getInstance();
    }


    @SuppressWarnings("unused")
    public static MessageFragment newInstance(int columnCount, ArrayList<String> messages) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_MESSAGES, messages);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            messages = getArguments().getStringArrayList(ARG_MESSAGES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new MessageRecyclerViewAdapter(mContext, messages);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    private void updateUI(final int position) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemInserted(position);
            }
        });
    }

    private void updateUI() {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

}
