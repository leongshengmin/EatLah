package com.eatlah.eatlah.fragments.customer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eatlah.eatlah.adapters.customer.HawkerCentreRecyclerViewAdapter;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.HawkerCentre;
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
public class HawkerCentreFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    // database and authentication instances
    private static final FirebaseDatabase mDb = FirebaseDatabase.getInstance("https://eatlah-fe598.firebaseio.com/");

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<HawkerCentre> mHCList;
    private HawkerCentreRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HawkerCentreFragment() {
    }

    @SuppressWarnings("unused")
    public static HawkerCentreFragment newInstance(int columnCount) {
        HawkerCentreFragment fragment = new HawkerCentreFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHCList = new ArrayList<>();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }


    /**
     * One time retrieval of hawker centres from db for display in recyclerView body
     */
    private void retrieveHawkerCentres() {
        System.out.println("retrieving hawker centres");
        DatabaseReference mDbRef = mDb.getReference(getResources().getString(R.string.hawker_centre_ref));
        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("retrieving...");
                System.out.println("datasnapshot: " + dataSnapshot);
                mHCList.clear();

                // add all hawker centres in db to hawkerCentreList
                for (DataSnapshot fcSnapshot : dataSnapshot.getChildren()) {
                    HawkerCentre hc = fcSnapshot.getValue(HawkerCentre.class);
                    System.out.println("Hawker centre: " + hc.get_id());
                    mHCList.add(hc);
                }

                notifyAdapter((Activity) mListener, mHCList);
                System.out.println("done!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("db", databaseError.getMessage());
            }
        });

    }

    /**
     * Notifies adapter to update recycler view due to change in dataset.
     * @param context context to update view.
     */
    public void notifyAdapter(Activity context, List<HawkerCentre> hawkerCentreList) {
        mHCList = hawkerCentreList;

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
        View view = inflater.inflate(R.layout.customer_fragment_hawkercentre_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

            mAdapter = new HawkerCentreRecyclerViewAdapter(mHCList, mListener);
            recyclerView.setAdapter(mAdapter);
            System.out.println("done setting adapter in hcfrag");
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveHawkerCentres();
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
        void onListFragmentInteraction(HawkerCentre hawkerCentre);
    }
}
