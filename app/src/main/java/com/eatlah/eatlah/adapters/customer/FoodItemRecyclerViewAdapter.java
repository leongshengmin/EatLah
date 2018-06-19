package com.eatlah.eatlah.adapters.customer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eatlah.eatlah.GlideApp;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.customer.FoodItemFragment.OnListFragmentInteractionListener;
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
 */
public class FoodItemRecyclerViewAdapter extends RecyclerView.Adapter<FoodItemRecyclerViewAdapter.ViewHolder> {

    private final List<FoodItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private FirebaseStorage mStorage;

    public FoodItemRecyclerViewAdapter(List<FoodItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mStorage = FirebaseStorage.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_view_holder_food_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FoodItem foodItem = mValues.get(position);

        if (foodItem.getImage_path() != null && !foodItem.getImage_path().isEmpty()) {
            glideImageInto(foodItem.getImage_path(), holder.mImageView);
        }
        holder.mNameView.setText(foodItem.getName());
        holder.mPriceView.setText(foodItem.getPrice());
        holder.mDescView.setText(foodItem.getDescription());
        System.out.println("done binding " + foodItem.getName() + " to view holder");

        holder.mAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                    String qty = holder.mQtyView.getText().toString();

                    if (qty != null && !qty.isEmpty()) {
                        int quantity = Integer.parseInt(qty);
                        mListener.onListFragmentInteraction(foodItem, quantity);
                    } else {
                        Toast.makeText((Activity)mListener, "invalid quantity!", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });
    }

    /**
     * Retrieves image of the corresponding file_name from cloud and glides it into the view.
     * @param file_name name of image (e.g. image_name.png)
     * @param imageView view to glide image into
     */
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
        private EditText mQtyView;
        private ImageView mImageView;
        private Button mAddToCart;


        public ViewHolder(View view) {
            super(view);
            mNameView = (TextView) view.findViewById(R.id.foodItemName_textView);
            mDescView = (TextView) view.findViewById(R.id.foodItemDesc_textView);
            mPriceView = (TextView) view.findViewById(R.id.price_textView);
            mQtyView = view.findViewById(R.id.orderqty_editText);
            mImageView = (ImageView) view.findViewById(R.id.foodItem_image);
            mAddToCart = view.findViewById(R.id.cart_button);
        }

    }
}
