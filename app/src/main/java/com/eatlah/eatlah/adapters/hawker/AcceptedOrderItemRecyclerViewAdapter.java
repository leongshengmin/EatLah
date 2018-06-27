package com.eatlah.eatlah.adapters.hawker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eatlah.eatlah.GlideApp;
import com.eatlah.eatlah.fragments.hawker.AcceptedOrderItemFragment.OnListFragmentInteractionListener;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AcceptedOrderItemRecyclerViewAdapter extends RecyclerView.Adapter<AcceptedOrderItemRecyclerViewAdapter.ViewHolder> {
    private static int GREEN = 0xFF00FA9A;
    private static int RED = 0xFFFFC0CB;
    private static int GREENCOLOR = -16713062;

    private final List<OrderItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private FirebaseStorage mStorage;
    private FragmentManager mFragmentManager;
    Order mOrder;

    public AcceptedOrderItemRecyclerViewAdapter(List<OrderItem> items, OnListFragmentInteractionListener listener, FragmentManager fragmentManager, Order order) {
        mValues = items;
        mListener = listener;
        mStorage = FirebaseStorage.getInstance();
        mFragmentManager = fragmentManager;
        mOrder = order;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hawker_view_holder_acceptedorderitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final OrderItem orderItem = mValues.get(position);

        if (orderItem.getImage_path() != null && !orderItem.getImage_path().isEmpty()) {
            glideImageInto(orderItem.getImage_path(), holder.mImageView);
        }
        holder.mNameView.setText(orderItem.getName());
        holder.mPriceView.setText(orderItem.getPrice());
        holder.mDescView.setText(orderItem.getDescription());
        holder.mQtyView.setText("" + orderItem.getQty()); // qty is int, cast to string
        holder.mCardView.setCardBackgroundColor(orderItem.isComplete() ? 0xFF00FA9A : 0xFFFFC0CB);
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmChangeStatus ccs = new ConfirmChangeStatus();
                ccs.orderItem = orderItem;
                ccs.position = position;
                ccs.context = AcceptedOrderItemRecyclerViewAdapter.this; // outer class
                ccs.holder = holder;
                ccs.show(mFragmentManager.beginTransaction(), "dialog");
            }
        });

    }



    private void glideImageInto(String file_name, final ImageView imageView) {
        System.out.println("gliding image " + file_name);
        StorageReference imgRef = mStorage.getReference(((Activity) mListener).getResources().getString(R.string.food_item_ref))
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
        private CardView mCardView;

        public ViewHolder(View view) {
            super(view);
            mNameView = view.findViewById(R.id.accepted_foodItemName_textView);
            mDescView = view.findViewById(R.id.accepted_foodItemDesc_textView);
            mPriceView = view.findViewById(R.id.accepted_price_textView);
            mQtyView = view.findViewById(R.id.accepted_orderqty_textView);
            mImageView = view.findViewById(R.id.accepted_foodItem_image);
            mCardView = view.findViewById(R.id.accepted_fooditem_view_holder);
        }

        public CardView getmCardView() { return mCardView; }
    }

}

class ConfirmChangeStatus extends DialogFragment {
    private static int GREEN = 0xFF00FA9A;
    private static int RED = 0xFFFFC0CB;
    private static int GREENCOLOR = -16713062;

    OrderItem orderItem;
    int position;
    AcceptedOrderItemRecyclerViewAdapter context;
    AcceptedOrderItemRecyclerViewAdapter.ViewHolder holder;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder _builder = new AlertDialog.Builder(getActivity());
        _builder.setMessage("Change order status?")
                .setTitle("Confirm Action")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Reference to the whole order
                        final DatabaseReference orderRef =
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("Orders")
                                        .child(context.mOrder.getTimestamp());
                        // Modify order/orderItem status
                        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Order order = dataSnapshot.getValue(Order.class);
                                List<OrderItem> updatedOrders = order.getOrders();

                                // Update orderitem status
                                OrderItem toMod = updatedOrders.get(position);
                                toMod.setComplete(!toMod.isComplete()); // Flip completion status

                                // Local gui changes
                                CardView cv = holder.getmCardView();
                                cv.setCardBackgroundColor( // If green, make red, otherwise green.
                                        cv.getCardBackgroundColor().getDefaultColor() == -16713062 ? RED : GREEN
                                );

                                // Update Order status
                                boolean allComplete = true;
                                Iterator<OrderItem> iter = updatedOrders.iterator();
                                while (allComplete && iter.hasNext()) { // If any item isn't complete, allComplete is false.
                                    if (!iter.next().isComplete()) allComplete = false;
                                }
                                if (allComplete) {
                                    order.setReady(true);
                                } else {
                                    order.setReady(false);
                                }

                                // Upload change on firebase
                                orderRef.setValue(order);
                            }
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    }})
                .setNeutralButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }});
        return _builder.create();
    }
}
