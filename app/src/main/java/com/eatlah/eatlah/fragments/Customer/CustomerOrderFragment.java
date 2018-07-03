package com.eatlah.eatlah.fragments.Customer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eatlah.eatlah.Cart;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.adapters.Customer.OrderRecyclerViewAdapter;
import com.eatlah.eatlah.models.OrderItem;
import com.eatlah.eatlah.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CustomerOrderFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String CART = "cart";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private OrderRecyclerViewAdapter mAdapter;
    private FloatingActionButton submit_btn;
    private static final FirebaseDatabase mDb = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseReference;
    private User mUser;
    private Cart cart;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CustomerOrderFragment() {
    }

    @SuppressWarnings("unused")
    public static CustomerOrderFragment newInstance(int columnCount, Cart cart) {
        CustomerOrderFragment fragment = new CustomerOrderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putSerializable(CART, cart);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * retrieves customer object async from db.
     */
    private void retrieveCustomerAsync() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDb.getReference(getResources().getString(R.string.user_ref))
                .child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("customer", "retrieved customer object");
                        mUser = dataSnapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("customer", databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retrieve customer object
        Thread t1 = new Thread() {
            @Override
            public void run() {
                retrieveCustomerAsync();
            }

        };
        t1.setName("customerAddress");
        t1.start();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            cart = (Cart) getArguments().getSerializable(CART);
        }
    }

    private void displayButton() {
        // hides fab
        FloatingActionButton fab = ((Activity)mListener).findViewById(R.id.fab);
        fab.hide();

        // displays the button to submit or cancel order
        submit_btn = ((Activity)mListener).findViewById(R.id.submit_or_cancel_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            submit_btn.setTooltipText(getButtonDisplayText());
        }
        submit_btn.show();
        getButtonDisplayText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment_order_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            displayButton();

            mAdapter = new OrderRecyclerViewAdapter(cart.getContents().getOrders(), mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    /**
     * returns a string corresponding to text displayed on orderView button
     * @return
     */
    private String getButtonDisplayText() {
        if (!alertCartEmpty()) {    // cart is not empty
            submit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // open pop up box for customer to specify collectionTime and self-collection/delivery
                    displayPopup();

                    // at this stage order is confirmed
                    saveOrderToDB();
                }
            });
            return ((Activity)mListener).getResources().getString(R.string.order_submit_button);
        }

        // cart is empty
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOrder();
            }
        });
        return ((Activity)mListener).getResources().getString(R.string.order_cancel_button);
    }

    private void saveOrderToDB() {
        DatabaseReference dbRef = mDb
                .getReference(getResources().getString(R.string.order_ref))
                .push();
        dbRef.setValue(cart.getContents())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("order to db", e.getMessage());
                    }
                });
    }

    /**
     * displays popup for customer to set delivery/collection time
     */
    private void displayPopup() {
        System.out.println("displaying popup");
        final View popupView = LayoutInflater.
                from(getActivity()).inflate(R.layout.main_dialog_custom_timepicker_layout, null)
                .findViewById(R.id.custom_dialog_layout);
        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        popupView.findViewById(R.id.submit_button)
                .setOnClickListener(new View.OnClickListener() {

                    private void setDeliveryOption() {
                        Spinner spinner = popupView.findViewById(R.id.deliveryOptions_spinner);
                        if (spinner.getSelectedItemPosition() == 0) {  // self-collection
                            cart.getContents().setSelf_collection(true);
                        } else {
                            cart.getContents().setSelf_collection(false);
                        }
                    }

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    private void setCollectionTime() {
                        TimePicker timePicker = popupView.findViewById(R.id.collectionTime_timePicker);
                        String time = formatTime(timePicker.getHour(), timePicker.getMinute());
                        cart.getContents().setCollectionTime(time);
                    }

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    /**
                     * displays popup window for customer to select delivery option,
                     * collection/delivery time.
                     */
                    public void onClick(View v) {
                        // customize order
                        setDeliveryOption();
                        setCollectionTime();
                        System.out.println("setting time and delivery option");
                        updateDb();

                        popupWindow.dismiss();

                        // order confirmed and updated
                        // display receipt
                        displayReceiptView();
                    }

                    /**
                     * displays the receipt corresponding to this customer's order
                     */
                    private void displayReceiptView() {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.remove(CustomerOrderFragment.this);
                        Fragment fragment = CustomerReceiptFragment.newInstance(cart.getContents(), retrieveCustomerAddress());
                        ft.add(fragment, "customerReceiptFragment");
                        ft.commit();
                    }

                    /**
                     * @return customer address associated with this order
                     */
                    private String retrieveCustomerAddress() {
                        if (mUser != null) return mUser.getAddress();

                        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                        for (Thread thread : threadSet) {
                            if (thread.getName().equals("customerAddress")) {
                                try {
                                    thread.join();
                                    return mUser.getAddress();
                                } catch (InterruptedException e) {
                                    Log.e("customer", e.getLocalizedMessage());
                                }
                            }
                        }
                        return null;

                    }
                });
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void updateDb() {
        databaseReference.child(cart.getContents().getTimestamp())
                .setValue(cart.getContents())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Submitted order successfully!", Toast.LENGTH_LONG).show();
                            //Snackbar.make(getView(), "Submitted order successfully!", Snackbar.LENGTH_SHORT).show();
                            // order confirmed and updated
                            // display receipt
                            displayReceiptView();
                        } else {
                            Log.e("db", task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * displays the receipt corresponding to this customer's order
     */
    private void displayReceiptView() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(CustomerOrderFragment.this);
        Fragment fragment = CustomerReceiptFragment.newInstance(cart.getContents(), retrieveCustomerAddress());
        ft.replace(R.id.frag_container, fragment);
        ft.commit();
    }

    /**
     * @return customer address associated with this order
     */
    private String retrieveCustomerAddress() {
        if (mUser != null) return mUser.getAddress();

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {
            if (thread.getName().equals("customerAddress")) {
                try {
                    thread.join();
                    return mUser.getAddress();
                } catch (InterruptedException e) {
                    Log.e("customer", e.getLocalizedMessage());
                }
            }
        }
        return null;
    }

    /**
     * processes input from timepicker and returns time in 24h format
     * @return
     */
    private String formatTime(int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        Format formatter = new SimpleDateFormat("HH:mm");
        String time = formatter.format(calendar.getTime());
        return time;
    }

    /**
     * cancels customer's order
     * empties cart and order session.
     */
    private void deleteOrder() {
        FirebaseDatabase
                .getInstance()
                .getReference(((Activity)mListener).getResources().getString(R.string.order_ref))
                .child(cart.getContents().getTimestamp())
                .setValue(null)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText((Activity)mListener, "Cancelled order.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("order", task.getException().getMessage());
                        }
                    }
                });
    }

    private boolean alertCartEmpty() {
        if (!cart.hasOrder() || cart.getContents() == null) {
            Toast.makeText((Activity) mListener, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            databaseReference = mDb.getReference(getResources().getString(R.string.order_ref));
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(OrderItem item);
    }

    public OrderRecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }
}
