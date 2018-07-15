package com.eatlah.eatlah.fragments.General;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    public ProfileFragment() {
        // Required empty public constructor
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView name_textView = view.findViewById(R.id.name_textView);
        name_textView.setText(mUser.getEmail().split("@")[0]);

        final EditText email_editText = view.findViewById(R.id.email_editText);
        email_editText.setHint(mUser.getEmail());

        final EditText address_editText = view.findViewById(R.id.deliveryAddress_editText);

        final EditText phone_editText = view.findViewById(R.id.phone_editText);
        phone_editText.setHint(mUser.getPhoneNumber());

        final ImageView img_imageView = view.findViewById(R.id.profileImg_imageView);

        img_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // upload image
            }
        });

        Button update_btn = view.findViewById(R.id.profileUpdate_btn);
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve details
                retrieveUpdatedContent();

            }

            private void retrieveUpdatedContent() {
                String address = address_editText.getText().toString();
                String phone = phone_editText.getText().toString();
                String email = email_editText.getText().toString();

                // retrieveImage();

                // update database
                updateDb(address, phone, email);
            }

            private void updateDb(final String address, final String phone, final String email) {
                final DatabaseReference databaseReference = mDb.getReference(getString(R.string.user_ref))
                        .child(mUser.getUid());

                databaseReference
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // retrieve existing user
                                User existingUser = dataSnapshot.getValue(User.class);

                                // update fields
                                if (!phone.isEmpty()) existingUser.set_id(phone);
                                if (!address.isEmpty()) existingUser.setAddress(address);
                                if (!email.isEmpty()) existingUser.setEmail(email);

                                // update db
                                databaseReference.setValue(existingUser);

                                Log.d("db", "Updated db profile successfully");
                                Snackbar.make(view, "Updated Profile Successfully!", Snackbar.LENGTH_SHORT)
                                        .show();
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
