package com.eatlah.eatlah.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.eatlah.eatlah.GlideApp;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.FoodItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ModifyMenuItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ModifyMenuItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModifyMenuItemFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private static final int RC_PHOTO_PICKER =  1;

    // Object related
    private String mFoodItemId;
    private FoodItem mFoodItem;

    private OnFragmentInteractionListener mListener;

    // DB Related
    private FirebaseDatabase mDb;
    private DatabaseReference mFoodItemsRef;
    private FirebaseStorage mStorage;
    private StorageReference mFoodItemsStorageRef;

    // Form related
    private EditText mPriceET;
    private EditText mNameET;
    private EditText mDescET;
    private ImageView mPicIV;
    private Button mBackBtn;
    private Button mSaveBtn;
    private String mImagePath;

    public ModifyMenuItemFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param foodItemId Food Item Id to modify
     * @return A new instance of fragment ModifyMenuItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ModifyMenuItemFragment newInstance(String foodItemId) {
        ModifyMenuItemFragment fragment = new ModifyMenuItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, foodItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFoodItemId = getArguments().getString(ARG_PARAM1);
        }
        loadDbRelatedFields(); // This will load into the view as well
        System.out.println("MMI onCreate done.");

    }

    private void loadDbRelatedFields() {
        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mFoodItemsRef = mDb.getReference("FoodItems");
        mFoodItemsStorageRef = mStorage.getReference("FoodItems");

        // Get the food item then load all the views
        mFoodItemsRef.child(mFoodItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                saveFoodItem(dataSnapshot.getValue(FoodItem.class)); // Saves in the field
                loadViewFields();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadViewFields() {
        View v = getView();
        mNameET = v.findViewById(R.id.hawker_mmi_name_editText);
        mPriceET = v.findViewById(R.id.hawker_mmi_price_editText);
        mDescET = v.findViewById(R.id.hawker_mmi_description_editText);
        mPicIV = v.findViewById(R.id.hawker_mmi_picture_imageView);
        mBackBtn = v.findViewById(R.id.hawker_mmi_back_button);
        mSaveBtn = v.findViewById(R.id.hawker_mmi_save_button);

        mNameET.setText(mFoodItem.getName());
        mPriceET.setText(mFoodItem.getPrice());
        mDescET.setText(mFoodItem.getDescription());
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed(); // Close this activity
            }
        });
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFoodItemsRef.child(mFoodItemId)
                             .setValue(new FoodItem(mFoodItemId,
                                                    mFoodItem.getStall_id(),
                                                    mNameET.getText().toString(),
                                                    mPriceET.getText().toString(),
                                                    mImagePath,
                                                    mDescET.getText().toString()));
                getActivity().onBackPressed(); // Close this activity
            }
        });
        mPicIV.setOnClickListener(new View.OnClickListener() { // Open image picker
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        // Last async task
        glideImage();
    }

    private void glideImage() {
        if (mImagePath != null && !mImagePath.isEmpty()) {
            mFoodItemsStorageRef.child(mImagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(mPicIV.getContext())
                            .load(uri.toString())
                            .into(mPicIV);
                }
            });
        }
    }

    public void saveFoodItem(FoodItem foodItem) {
        mFoodItem = foodItem;
        mImagePath = foodItem.getImage_path();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hawker_fragment_modifymenuitem, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // if file from phone was blah/asdf.jpg, store as FoodItems/asdf.jpg
            final StorageReference photoRef = mFoodItemsStorageRef.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When successful, get download url
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mImagePath = uri.getLastPathSegment().toString().split("/")[1]; // The path is "FoodItems/ID", so return just ID

                                    System.out.println("uri: " + uri.toString() + ", lastPath: " + uri.getLastPathSegment().toString().split("/")[1]);
                                    glideImage();
                                }
                            });
                        }
                    });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
