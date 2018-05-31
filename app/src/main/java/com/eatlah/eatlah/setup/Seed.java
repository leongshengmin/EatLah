package com.eatlah.eatlah.setup;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eatlah.eatlah.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Seed {

    private FirebaseDatabase mDb;
    private DatabaseReference mDbRef;

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private Activity context;

    public Seed(Activity context) {
        this.context = context;
        // Initialize firebase db
        mDb = FirebaseDatabase.getInstance();
        mDbRef = mDb.getReference(context.getResources().getString(R.string.hawker_centre_ref));

        // Initialize firebase cloud
        mStorage = FirebaseStorage.getInstance();
    }

    public void run() {
        uploadHCFiles();
        uploadHSFiles();
    }

    /**
     * Uploads image at image_path to the cloud and returns true upon success, false otherwise.
     * @param image_path path specified where image can be found.
     * @return a boolean rep success of upload.
     */
    private boolean uploadImage(String image_path) {
        try {
            InputStream stream = new FileInputStream(new File(image_path));
            UploadTask uploadTask = mStorageRef.putStream(stream);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d("cloud upload", "Successfully uploaded hawker centre image to cloud");
                    } else {
                        Log.e("cloud upload", task.getException().getMessage());
                    }
                }
            });
            stream.close();
            return uploadTask.isSuccessful();
        } catch (FileNotFoundException e) {
            Log.e("cloud upload", e.getMessage());
        } catch (IOException e) {
            Log.e("cloud upload", e.getMessage());
        }
        return false;
    }

    // for non-seeded data, we first add the data to db then add the image_path to storage
    /**
     * For seeded data we will retrieve the seeded data from db and get the image_path.
     * Then put the image at the image_path to storage.
     */
    private void uploadHCFiles() {
        mStorageRef = mStorage.getReference(context.getResources().getString(R.string.hawker_centre_ref));
        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println("snapshot of hc in seeding: " + snapshot);
                    System.out.println("image path: " + snapshot.child("image_path").getValue(String.class));
                    String image_path = snapshot
                            .child("image_path")
                            .getValue(String.class);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void uploadHSFiles() {
        mDbRef = mDbRef.child(context.getResources().getString(R.string.hawker_stall_ref));
        mStorageRef = mStorage.getReference(context.getResources().getString(R.string.hawker_stall_ref));

        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot nestedSnap: snapshot.getChildren()) {
                        String image_path = nestedSnap.child("image_path")
                                .getValue(String.class);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("db", databaseError.getMessage());
            }
        });
    }
}
