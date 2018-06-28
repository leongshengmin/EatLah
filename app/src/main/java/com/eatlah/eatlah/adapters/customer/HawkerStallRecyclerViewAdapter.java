package com.eatlah.eatlah.adapters.customer;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eatlah.eatlah.GlideApp;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.Customer.CustomerHawkerStallFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerHawkerStallFragment.OnListFragmentInteractionListener;
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
public class HawkerStallRecyclerViewAdapter extends RecyclerView.Adapter<HawkerStallRecyclerViewAdapter.ViewHolder> {

    // typeface
    private Typeface typefaceRaleway;

    // fields for setting adapter
    private List<HawkerStall> mValues;
    private OnListFragmentInteractionListener mListener;

    // firebase storage instances
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    public HawkerStallRecyclerViewAdapter(List<HawkerStall> mHSlist, CustomerHawkerStallFragment.OnListFragmentInteractionListener listener) {
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
                .inflate(R.layout.customer_view_holder_hawker_stall, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        HawkerStall hs = mValues.get(position);
        holder.mHSAddressView.setTypeface(typefaceRaleway);
        String stallNumberPlaceholder = ((Activity) mListener).getResources().getString(R.string.stall_number);
        holder.mHSAddressView.setText(String.format(stallNumberPlaceholder, hs.get_id()));

        holder.mHSNameView.setTypeface(typefaceRaleway);
        holder.mHSNameView.setText(hs.getStall_name());

        if (hs.getImage_path() != null && !hs.getImage_path().isEmpty()) glideImageInto(hs.getImage_path(), holder.mHSImageView);

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
        public TextView mHSNameView;
        public TextView mHSAddressView;

        public ViewHolder(View view) {
            super(view);
            mHSImageView = (ImageView) view.findViewById(R.id.foodItem_image);
            mHSNameView = (TextView) view.findViewById(R.id.foodItemName_textView);
            mHSAddressView = (TextView) view.findViewById(R.id.foodItemDesc_textView);
        }
    }
}
