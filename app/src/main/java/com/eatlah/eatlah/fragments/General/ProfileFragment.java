package com.eatlah.eatlah.fragments.General;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.Courier.CourierHomepage;
import com.eatlah.eatlah.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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

 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private Activity mListener;
    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mDb;
    private final FirebaseUser mUser;
    private final FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private static final int PICK_IMAGE_REQUEST = 71;

    private Uri filePath;
    private Snackbar imageUploadPrompt;

    public ProfileFragment() {
        // Required empty public constructor
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        final EditText name_editText = view.findViewById(R.id.name_editText);
        name_editText.setHint(mUser.getEmail().split("@")[0]);

        final EditText email_editText = view.findViewById(R.id.email_editText);
        email_editText.setHint(mUser.getEmail());

        final EditText address_editText = view.findViewById(R.id.deliveryAddress_editText);

        final EditText phone_editText = view.findViewById(R.id.phone_editText);
        phone_editText.setHint(mUser.getPhoneNumber());

        final ImageView img_imageView = view.findViewById(R.id.profileImg_imageView);

        img_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CourierHomepage) mListener).clearViewsInContentView();

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Update Profile Picture"), PICK_IMAGE_REQUEST);
            }
        });

        final Button update_btn = view.findViewById(R.id.profileUpdate_btn);
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve details
                retrieveUpdatedContent();

            }

            private void retrieveUpdatedContent() {
                retrieveImageUpload(img_imageView);

                String name = name_editText.getText().toString();
                String address = address_editText.getText().toString();
                String phone = phone_editText.getText().toString();
                String email = email_editText.getText().toString();

                // update database
                updateDb(name, address, phone, email);
            }

            private void retrieveImageUpload(ImageView imageView) {
                if (filePath != null) {
                    String imgName = String.format("%s.png", mUser.getUid());
                    mStorageReference = mStorage.getReference(mListener.getString(R.string.user_ref));
                    StorageReference childRef = mStorageReference.child(imgName);

                    UploadTask uploadTask = childRef.putFile(filePath);

                    imageUploadPrompt.dismiss();

                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(view, "Successfully uploaded image!", Snackbar.LENGTH_SHORT)
                                        .show();
                            } else {
                                Log.e("upload img", task.getException().getMessage());
                            }
                        }
                    });
                } else {
                    // no existing profile picture
                    if (mUser.getPhotoUrl() == null) {
                        imageUploadPrompt = Snackbar.make(view, "Upload an image.", Snackbar.LENGTH_INDEFINITE);
                        imageUploadPrompt.show();
                    } else {    // user intentionally left profile img field blank
                        Glide.with(mListener)
                                .load(mUser.getPhotoUrl())
                                .into(img_imageView);
                    }
                }
            }

            /**
             * @param requestCode
             * @param resultCode
             * @param data
             */
           protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                    filePath = data.getData();

                    try {
                        //getting image from gallery
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mListener.getContentResolver(), filePath);

                        //Setting image to ImageView
                        img_imageView.setImageBitmap(bitmap);
                        Log.d("set image", "done setting img in imageview");

                    } catch (Exception e) {
                        Log.e("set image", e.getMessage());
                    }
                }
            }

            private void updateDb(final String name, final String address, final String phone, final String email) {
                final DatabaseReference databaseReference = mDb.getReference(getString(R.string.user_ref))
                        .child(mUser.getUid());

                databaseReference
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // retrieve existing user
                                User existingUser = dataSnapshot.getValue(User.class);

                                // update fields
                                if (!TextUtils.isEmpty(phone) && TextUtils.isEmpty(phone)) {
                                    existingUser.set_id(phone);
                                }
                                if (!address.isEmpty()) {
                                    existingUser.setAddress(address);
                                }
                                if (!TextUtils.isEmpty(email) && email.contains("@")) {
                                    existingUser.setEmail(email);
                                }

                                updateProfile(existingUser);

                                // update db
                                databaseReference.setValue(existingUser);

                                Log.d("db", "Updated db profile successfully");
                                Snackbar.make(view, "Updated Profile Successfully!", Snackbar.LENGTH_SHORT)
                                        .show();
                            }

                            private void updateProfile(User user) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .setPhotoUri((filePath == null) ? mUser.getPhotoUrl() : filePath)
                                        .build();
                                mUser.updateProfile(profileUpdates);
                                mUser.updateEmail(user.getEmail());

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("db", databaseError.getMessage());
                            }
                        });
            }
        });

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
