package com.eatlah.eatlah.adapters.hawker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eatlah.eatlah.GlideApp;
import com.eatlah.eatlah.fragments.hawker.MenuItemFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.FoodItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FoodItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MenuItemRecyclerViewAdapter extends RecyclerView.Adapter<MenuItemRecyclerViewAdapter.ViewHolder> {

    private final List<FoodItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private FirebaseStorage mStorage;

    public MenuItemRecyclerViewAdapter(List<FoodItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hawker_view_holder_menuitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mMenuItemDescriptionTextView.setText(mValues.get(position).getDescription());
        holder.mMenuItemItemNameTextView.setText(mValues.get(position).getName());
        holder.mMenuItemPriceTextView.setText(mValues.get(position).getPrice());

        // No idea what this is
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        if (holder.mItem.getImage_path() != null && !holder.mItem.getImage_path().isEmpty()) {
            glideImageInto(holder.mItem.getImage_path(), holder.mMenuItemImageView);
        }
    }

    private void glideImageInto(String image_path, final ImageView mMenuItemImageView) {
        StorageReference imgRef = mStorage.getReference("FoodItems")
                .child(image_path);
        try {
            final File tmpFile = File.createTempFile("images", "jpg");
            imgRef.getFile(tmpFile)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                GlideApp.with((Activity) mListener)
                                        .load(tmpFile)
                                        .into(mMenuItemImageView);
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
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mMenuItemDescriptionTextView;
        private final TextView mMenuItemItemNameTextView;
        private final TextView mMenuItemPriceTextView;
        private final ImageView mMenuItemImageView;
        public FoodItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMenuItemDescriptionTextView = (TextView) view.findViewById(R.id.MenuItemDescriptionTextView);
            mMenuItemItemNameTextView = (TextView) view.findViewById(R.id.MenuItemItemNameTextView);
            mMenuItemPriceTextView = (TextView) view.findViewById(R.id.MenuItemPriceTextView);
            mMenuItemImageView = (ImageView) view.findViewById(R.id.MenuItemImageView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMenuItemItemNameTextView.getText() + "'";
        }
    }
}
