package com.eatlah.eatlah.adapters;

import android.app.Activity;
import android.content.res.AssetManager;
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
import com.eatlah.eatlah.fragments.HawkerCentreFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.models.HawkerCentre;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link HawkerCentre} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HawkerCentreRecyclerViewAdapter extends RecyclerView.Adapter<HawkerCentreRecyclerViewAdapter.ViewHolder> {

    private final List<HawkerCentre> mHClist;
    private final OnListFragmentInteractionListener mListener;

    // typefaces
    private AssetManager assetManager;
    private Typeface typefaceRaleway;

    // cloud storage instance to store uploads
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    public HawkerCentreRecyclerViewAdapter(List<HawkerCentre> mHClist, OnListFragmentInteractionListener listener) {
        System.out.println("in hc adapter constructor");
        this.mHClist = mHClist;
        mListener = listener;

        Activity context = (Activity) listener;
        // Initialize firebase cloud
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference(context.getResources().getString(R.string.hawker_centre_ref));

        // Initialize fonts
        typefaceRaleway = Typeface.createFromAsset(context.getAssets(), "Raleway-Medium.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        System.out.println("Creating view holder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_view_holder_hawker_centre, parent, false);
        System.out.println("inflated view holder");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        System.out.println("binding view holder");
        final HawkerCentre hc = mHClist.get(position);

        // set the contents of viewholder
        if (hc.getImage_path() != null && !hc.getImage_path().isEmpty()) glideImageInto(hc.getImage_path(), holder.mHCImageView);
        System.out.println("done gliding image");

        holder.mHCAddressView.setText(hc.get_id());
        holder.mHCAddressView.setTypeface(typefaceRaleway);

        holder.mHCNameView.setText(hc.getName());
        holder.mHCNameView.setTypeface(typefaceRaleway);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(hc);
            }
        });

        System.out.println("done binding components in hc viewholder");
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

    @Override
    public int getItemCount() {
        return mHClist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mHCImageView;
        private TextView mHCNameView;
        private TextView mHCAddressView;

        public ViewHolder(View view) {
            super(view);
            mHCImageView = view.findViewById(R.id.hcImage);
            mHCNameView = view.findViewById(R.id.orderTimestamp_testView);
            mHCAddressView = view.findViewById(R.id.orderTime_textView);
        }
    }
}
