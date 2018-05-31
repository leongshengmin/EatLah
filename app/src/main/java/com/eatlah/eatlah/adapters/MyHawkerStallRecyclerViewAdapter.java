package com.eatlah.eatlah;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.eatlah.eatlah.HawkerStallFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.models.HawkerStall;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link HawkerStall} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyHawkerStallRecyclerViewAdapter extends RecyclerView.Adapter<MyHawkerStallRecyclerViewAdapter.ViewHolder> {

    // typeface
    private Typeface typefaceRaleway;

    // fields for setting adapter
    private List<HawkerStall> mValues;
    private OnListFragmentInteractionListener mListener;

    // firebase storage instances
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    public MyHawkerStallRecyclerViewAdapter(List<HawkerStall> mHSlist, HawkerStallFragment.OnListFragmentInteractionListener listener) {
        mValues = mHSlist;
        mListener = listener;

        Activity context = (Activity) listener;
        // Initialize firebase cloud
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference(context.getResources().getString(R.string.hawker_stall_ref));

        // Initialize fonts
        typefaceRaleway = Typeface.createFromAsset(context.getAssets(), "Raleway-Medium.ttf");
    }

    // activity for retrieving resources
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hawker_stall_view_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        HawkerStall hs = mValues.get(position);
        holder.mHSAddressView.setTypeface(typefaceRaleway);
        String stallNumberPlaceholder = ((Activity) mListener).getResources().getString(R.string.stall_number);
        holder.mHSAddressView.setText(String.format(stallNumberPlaceholder, hs.getStallNumber()));

        holder.mHSNameView.setTypeface(typefaceRaleway);
        holder.mHSNameView.setText(hs.getName());

        glideImageInto(hs.getImagePath(), holder.mHSImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mValues.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Retrieves image of the corresponding file_name from cloud and glides it into the view.
     * @param file_name name of image (e.g. image_name.png)
     * @param imageView view to glide image into
     */
    private void glideImageInto(String file_name, final ImageView imageView) {
        System.out.println("gliding image " + file_name);
        StorageReference imgRef = mStorageRef.child(file_name);

        try {
            final File tmpFile = File.createTempFile("images", "jpg");
            imgRef.getFile(tmpFile)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                GlideApp.with((Activity) mListener)
                                        .load(tmpFile)
                                        .into(imageView);
                                System.out.println("successfully loaded image");
                            } else {
                                Log.e("glide", task.getException().getMessage());
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mHSImageView;
        public EditText mHSNameView;
        public EditText mHSAddressView;

        public ViewHolder(View view) {
            super(view);
            mHSImageView = (ImageView) view.findViewById(R.id.hsImage);
            mHSNameView = (EditText) view.findViewById(R.id.hsName_editText);
            mHSAddressView = (EditText) view.findViewById(R.id.hsAddress_editText);
        }
    }
}
