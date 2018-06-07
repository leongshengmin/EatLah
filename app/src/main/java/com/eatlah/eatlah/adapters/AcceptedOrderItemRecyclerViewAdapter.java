package com.eatlah.eatlah.adapters;

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
import com.eatlah.eatlah.fragments.AcceptedOrderItemFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AcceptedOrderItemRecyclerViewAdapter extends RecyclerView.Adapter<AcceptedOrderItemRecyclerViewAdapter.ViewHolder> {
    private final List<OrderItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private FirebaseStorage mStorage;

    public AcceptedOrderItemRecyclerViewAdapter(List<OrderItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hawker_view_holder_acceptedorderitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final OrderItem orderItem = mValues.get(position);

        if (orderItem.getImage_path() != null && !orderItem.getImage_path().isEmpty()) {
            glideImageInto(orderItem.getImage_path(), holder.mImageView);
        }
        holder.mNameView.setText(orderItem.getName());
        holder.mPriceView.setText(orderItem.getPrice());
        holder.mDescView.setText(orderItem.getDescription());
        holder.mQtyView.setText("" + orderItem.getQty()); // qty is int, cast to string
    }

    private void glideImageInto(String file_name, final ImageView imageView) {
        System.out.println("gliding image " + file_name);
        StorageReference imgRef = mStorage.getReference(((Activity)mListener).getResources().getString(R.string.food_item_ref))
                .child(file_name);

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
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mNameView;
        private TextView mDescView;
        private TextView mPriceView;
        private TextView mQtyView;
        private ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mNameView = view.findViewById(R.id.accepted_foodItemName_textView);
            mDescView = view.findViewById(R.id.accepted_foodItemDesc_textView);
            mPriceView = view.findViewById(R.id.accepted_price_textView);
            mQtyView = view.findViewById(R.id.accepted_orderqty_textView);
            mImageView = view.findViewById(R.id.accepted_foodItem_image);
        }
    }
}
