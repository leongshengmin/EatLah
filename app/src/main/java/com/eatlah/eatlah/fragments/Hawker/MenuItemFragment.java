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
import com.eatlah.eatlah.adapters.Hawker.MenuItemRecyclerViewAdapter;
import com.eatlah.eatlah.models.FoodItem;
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
public class MenuItemFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private MenuItemRecyclerViewAdapter mAdapter;
    private FirebaseDatabase mDb;
    private DatabaseReference mFoodItems;
    public static User mUser;
    List<FoodItem> mFoodItemList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuItemFragment() {
    }

    @SuppressWarnings("unused")
    public static MenuItemFragment newInstance(int columnCount) {
        MenuItemFragment fragment = new MenuItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        System.out.println("Made a new instance");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mDb = FirebaseDatabase.getInstance();
        mFoodItems = mDb.getReference("FoodItems");
        mFoodItemList = new ArrayList<>();
        findUserAndRetrieveMenu();
    }

    private void findUserAndRetrieveMenu() {
        String uid = FirebaseAuth.getInstance().getUid();
        mDb.getReference("users")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User currUser = dataSnapshot.getValue(User.class);
                        storeUser(currUser);
                        retrieveItems();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void retrieveItems() {
        DatabaseReference mDbRef = mDb.getReference("FoodItems");
        final String hawkerId = mUser.get_hawkerId();
        System.out.println("Hawker id: " + hawkerId);
        mDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFoodItemList.clear();
                // Look through all food items and find those from this hawker
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    FoodItem fi = ds.getValue(FoodItem.class);
                    // If belongs to the user, add it
                    if (fi.getStall_id().equals(hawkerId)) {
                        mFoodItemList.add(fi);
                    }
                }
                if (mListener == null) mListener = (OnListFragmentInteractionListener) getContext();
                ((Activity) mListener).runOnUiThread(new Runnable() {
                    @Override
                    public void run() { mAdapter.notifyDataSetChanged(); }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void storeUser(User user) { mUser = user; }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hawker_fragment_menuitem, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new MenuItemRecyclerViewAdapter(mFoodItemList, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(FoodItem item);
    }
}
