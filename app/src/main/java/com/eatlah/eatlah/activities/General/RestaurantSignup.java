package com.eatlah.eatlah.activities.General;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.HawkerStall;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RestaurantSignup extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER =  1;
    // Fields
    private AutoCompleteTextView mHawkerCentreId;
    private EditText mHawkerStallId;
    private EditText mHawkerName;
    private EditText mHawkerOwner;
    // Button
    private Button mBackBtn, mConfirmBtn;
    // Database
    private DatabaseReference mHawkerStallsReference;
    private FirebaseDatabase mDb;
    // Autofill
    private ArrayList<String> hawkers;
    private String[] HAWKERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_restaurant_signup);
        System.out.println("YOLO");
        // Database
        mDb = FirebaseDatabase.getInstance();
        mHawkerStallsReference  = mDb.getReference("HawkerStalls");
        // Views/buttons
        mHawkerCentreId = findViewById(R.id.hawkerSuggestions_textView);
        mHawkerStallId = findViewById(R.id.hawkerId_editText);
        mHawkerName = findViewById(R.id.hawkerName_editText);
        mHawkerOwner = findViewById(R.id.hawkerOwner_editText);
        mBackBtn = findViewById(R.id.hawker_stall_back_button);
        mConfirmBtn = findViewById(R.id.hawker_stall_confirm_button);
        // Interactions
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RestaurantSignup.this, "Changes not saved", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        // Autocomplete Hawker Centres
        hawkers = new ArrayList<>();
        mHawkerStallsReference.addListenerForSingleValueEvent(new ValueEventListener() { // get hawkers
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) { // get all hawker centre ids
                    saveHawkerCentre(ds.getKey());
                }
                fixHawkerAdapter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        System.out.println("Hawkers: " + hawkers);

    }

    /**
     * Callback after hawker centre IDs are obtained.
     */
    private void fixHawkerAdapter() {
        HAWKERS = hawkers.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, HAWKERS);
        mHawkerCentreId.setAdapter(adapter);
        System.out.println(hawkers);
    }

    /**
     * Used to save hawkerCentres into the arraylist without using final
     * @param hawkerCentre the ID of the hawkercentre to save.
     */
    private void saveHawkerCentre(String hawkerCentre) {
        hawkers.add(hawkerCentre);
    }

    /**
     * Called when confirm button is pressed.
     */
    private void signUp() {
        // Add to the hawkercentre node a new hawker stall
        mHawkerStallsReference
                .child(mHawkerCentreId.getText().toString())
                .child(mHawkerStallId.getText().toString())
                .setValue(new HawkerStall(mHawkerCentreId.getText().toString(),
                                          mHawkerStallId.getText().toString(),
                                          mHawkerName.getText().toString(),
                                          mHawkerOwner.getText().toString(),
                                          null,
                                          "no_image.jpg"));
        Toast.makeText(this, "Hawker stall signup successful!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
