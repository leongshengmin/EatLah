package com.eatlah.eatlah.fragments.Hawker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.HawkerStall;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateDetailsFragment extends Fragment {
    public static int RC_PHOTO_PICKER = 1;

    // Arguments
    private static final String msHawkerStallId = "param1";
    private static final String msUserId = "param2";
    private static final String msHawkerId = "param3";
    private String mHawkerStallId;
    private String mUserId;
    private String mHawkerId;

    private OnFragmentInteractionListener mListener;

    // Views
    private ImageView   mImageView;
    private TextView    mNameTextView;
    private HawkerStall mHawkerStall;
    private String      mImagePath;
    private Button      mBackBtn;
    private Button      mSaveChangesBtn;

    //Database
    private FirebaseStorage     mStorage;
    private FirebaseDatabase    mDb;
    private StorageReference    mHawkerStallsPhotosRef;
    private DatabaseReference   mHawkerStallsRef;

    public UpdateDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param hawkerStallId Hawker stall id to update details
     * @param userId User who is trying to update (For rights check)
     * @return A new instance of fragment UpdateDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdateDetailsFragment newInstance(String hawkerStallId, String hawkerId, String userId) {
        UpdateDetailsFragment fragment = new UpdateDetailsFragment();
        Bundle args = new Bundle();
        args.putString(msHawkerStallId, hawkerStallId);
        args.putString(msHawkerId, hawkerId);
        args.putString(msUserId, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHawkerStallId  = getArguments().getString(msHawkerStallId);
            mUserId         = getArguments().getString(msUserId);
            mHawkerId       = getArguments().getString(msHawkerId);
        }
        mDb                     = FirebaseDatabase.getInstance();
        mStorage                = FirebaseStorage.getInstance();
        mHawkerStallsRef        = mDb.getReference("HawkerStalls");
        mHawkerStallsPhotosRef  = mStorage.getReference("HawkerStalls");

        mHawkerStallsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HawkerStall hs  = dataSnapshot
                                        .child(mHawkerId)
                                        .child(mHawkerStallId)
                                        .getValue(HawkerStall.class);
                mHawkerStall    = hs;
                mImagePath      = hs.getImage_path();
                fillViews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    private void fillViews() {
        // Store references
        View v = getView();
        mImageView      = v.findViewById(R.id.hawker_updatedetails_picture_imageView);
        mNameTextView   = v.findViewById(R.id.hawker_updatedetails_name_editText);
        mBackBtn        = v.findViewById(R.id.hawker_updatedetails_back_button);
        mSaveChangesBtn = v.findViewById(R.id.hawker_updatedetails_save_button);
        // Fill them up
        mNameTextView.setText(mHawkerStall.getStall_name());
        mSaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHawkerStallsRef
                        .child(mHawkerId)
                        .child(mHawkerStallId)
                        .child("stall_name")
                        .setValue(mNameTextView.getText().toString());
                mHawkerStallsRef
                        .child(mHawkerId)
                        .child(mHawkerStallId)
                        .child("image_path")
                        .setValue(mImagePath);
                Toast   .makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
                getActivity()
                        .onBackPressed();
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity()
                        .onBackPressed();
                Toast   .makeText(getContext(), "Changes not saved.", Toast.LENGTH_SHORT).show();
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        glideImage();
    }

    private void glideImage() {
        if (mImagePath != null && !mImagePath.isEmpty()) {
            mHawkerStallsPhotosRef
                    .child(mImagePath)
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                        Glide.with(mImageView.getContext())
                             .load(uri.toString())
                             .into(mImageView);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hawker_fragment_update_details, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // if file from phone was blah/asdf.jpg, store as FoodItems/asdf.jpg
            final StorageReference photoRef = mHawkerStallsPhotosRef.child(selectedImageUri.getLastPathSegment());
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
