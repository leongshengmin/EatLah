package com.eatlah.eatlah.adapters.Customer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eatlah.eatlah.GlideApp;
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

public class FoodItemArrayAdapter extends ArrayAdapter<FoodItem> {

    private Activity context;
    private List<FoodItem> foodItemList;

    private FirebaseStorage mStorage;

    public FoodItemArrayAdapter(Activity context, List<FoodItem> foodItemList) {
        super(context, android.R.layout.simple_list_item_1, foodItemList);
        this.context = context;
        this.foodItemList = foodItemList;
        this.mStorage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View viewHolder = convertView;
        if (convertView == null) {
            viewHolder = context.getLayoutInflater().inflate(R.layout.customer_view_holder_hawker_stall, parent, false);
        }

        FoodItem foodItem = foodItemList.get(position);
        ((TextView) viewHolder.findViewById(R.id.orderName_textView))
                .setText(foodItem.getName());
        ((TextView) viewHolder.findViewById(R.id.orderTime_textView))
                .setText(foodItem.getDescription());
        ((TextView) viewHolder.findViewById(R.id.price_textView))
                .setText(context.getResources().getString(R.string.food_item_price));
        ImageView imageView = ((ImageView) viewHolder.findViewById(R.id.hcImage));
        if (foodItem.getImage_path() != null && !foodItem.getImage_path().isEmpty()) {
            glideImageInto(foodItem.getImage_path(), imageView);
        }
        System.out.println("food item stall id: " + foodItem.getStall_id());

        return viewHolder;
    }

    /**
     * Retrieves image of the corresponding file_name from cloud and glides it into the view.
     * @param file_name name of image (e.g. image_name.png)
     * @param imageView view to glide image into
     */
    private void glideImageInto(String file_name, final ImageView imageView) {
        System.out.println("gliding image " + file_name);
        StorageReference imgRef = mStorage.getReference(context.getResources().getString(R.string.food_item_ref))
                .child(file_name);

        try {
            final File tmpFile = File.createTempFile("images", "jpg");
            imgRef.getFile(tmpFile)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                GlideApp.with(context)
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
}
