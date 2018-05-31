package com.eatlah.eatlah.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eatlah.eatlah.adapters.MyHawkerStallRecyclerViewAdapter;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.HawkerCentre;
import com.eatlah.eatlah.models.HawkerStall;
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
public class HawkerStallFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<HawkerStall> mHSList;
    private MyHawkerStallRecyclerViewAdapter mAdapter;
    private static HawkerCentre hawkerCentre;

    private FirebaseDatabase mDb;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HawkerStallFragment() {
    }

    @SuppressWarnings("unused")
    public static HawkerStallFragment newInstance(int columnCount, HawkerCentre hc) {
        hawkerCentre = hc;
        HawkerStallFragment fragment = new HawkerStallFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHSList = new ArrayList<>();
        mDb = FirebaseDatabase.getInstance();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hawkerstall_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new MyHawkerStallRecyclerViewAdapter(mHSList, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveHawkerStalls();
    }

    private void retrieveHawkerStalls() {
        // set database ref to hawkerStalls/:hawkerCentre_id
        DatabaseReference dbRef = mDb.getReference(getResources().getString(R.string.hawker_stall_ref))
                .child(hawkerCentre.get_id());

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {

            /**
             * Retrieve the children nodes of this path.
             * This will give us the hawkerStalls associated with this hawkerCentre obj
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("retrieving...");
                System.out.println("datasnapshot: " + dataSnapshot);
                mHSList.clear();

                // add all hawker centres in db to hawkerCentreList

                for (DataSnapshot fcSnapshot : dataSnapshot.getChildren()) {
                    System.out.println("fc snapshot: " + fcSnapshot);
                    HawkerStall hs = fcSnapshot.getValue(HawkerStall.class);
                    mHSList.add(hs);
                }

                notifyAdapter((Activity) mListener, mHSList);
                System.out.println("done!");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
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
     * Notifies adapter to update recycler view due to change in dataset.
     * @param context context to update view.
     */
    public void notifyAdapter(Activity context, List<HawkerStall> hawkerStallList) {
        mHSList = hawkerStallList;

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
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
        void onListFragmentInteraction(HawkerStall item);
    }
}
