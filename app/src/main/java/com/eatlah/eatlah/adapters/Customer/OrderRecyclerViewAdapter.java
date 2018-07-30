package com.eatlah.eatlah.adapters.Customer;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.Customer.CustomerHomepage;
import com.eatlah.eatlah.fragments.Customer.CustomerOrderFragment;
import com.eatlah.eatlah.listeners.OnSwipeTouchListener;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DecimalFormat;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link CustomerOrderFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class OrderRecyclerViewAdapter extends RecyclerView.Adapter<OrderRecyclerViewAdapter.ViewHolder> {

    private final List<OrderItem> mValues;
    private final CustomerOrderFragment.OnListFragmentInteractionListener mListener;

    public OrderRecyclerViewAdapter(List<OrderItem> items, CustomerOrderFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_view_holder_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final OrderItem orderItem = mValues.get(position);
        holder.mName.setText(orderItem.getName());
        holder.mDesc.setText(orderItem.getDescription());

        // calculate price and set text
        calculatePrice(orderItem, holder.mPrice);

        holder.mQty.setText(Integer.toString(orderItem.getQty()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListFragmentInteraction(orderItem);
            }
        });

        // left swipe decrements the quantity of order item by 1
        // right swipe increments qty of order item by 1
        holder.itemView.setOnTouchListener(new OnSwipeTouchListener((Activity) mListener, this, orderItem));


        holder.mCancelButton.setText("Remove All");
        holder.mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CustomerHomepage) mListener).getOrder()
                        .removeAll(orderItem, OrderRecyclerViewAdapter.this);
                notifyItemRemoved(position);
            }
        });

        if (orderItem.getImage_path() != null && !orderItem.getImage_path().isEmpty()) {
            FirebaseStorage
                    .getInstance()
                    .getReference("FoodItems")
                    .child(orderItem.getImage_path())
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.mImageView.getContext())
                         .load(uri.toString())
                         .into(holder.mImageView);
                }
            });
        }
    }

    private void calculatePrice(OrderItem orderItem, TextView priceView) {
        System.out.println("ordered item: " + orderItem);
        double price = orderItem.getQty() * Double.parseDouble(orderItem.getPrice());
        DecimalFormat df = new DecimalFormat("##.00");
        priceView.setText(String.format("$%s", df.format(price)));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mName;
        private TextView mQty;
        private TextView mPrice;
        private TextView mDesc;
        private Button mCancelButton;
        private ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.orderName_textView);
            mQty = (TextView) view.findViewById(R.id.orderQty_textView);
            mPrice = view.findViewById(R.id.price_textView);
            mDesc = view.findViewById(R.id.orderAddress_textView);
            mCancelButton = view.findViewById(R.id.viewOrders_button);
            mImageView = view.findViewById(R.id.orderItem_image);
        }

    }
}
