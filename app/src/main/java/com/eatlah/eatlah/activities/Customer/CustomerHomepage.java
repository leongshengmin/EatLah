package com.eatlah.eatlah.activities.Customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.eatlah.eatlah.Cart;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.General.LoginActivity;
import com.eatlah.eatlah.fragments.Customer.CustomerFoodItemFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerHawkerStallFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerOrderFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerReceiptFragment;
import com.eatlah.eatlah.fragments.Customer.HawkerCentreFragment;
import com.eatlah.eatlah.fragments.General.PastOrdersFragment;
import com.eatlah.eatlah.fragments.General.ProfileFragment;
import com.eatlah.eatlah.models.FoodItem;
import com.eatlah.eatlah.models.HawkerCentre;
import com.eatlah.eatlah.models.HawkerStall;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.eatlah.eatlah.fragments.Hawker.MenuItemFragment.mUser;

public class CustomerHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HawkerCentreFragment.OnListFragmentInteractionListener,
        CustomerHawkerStallFragment.OnListFragmentInteractionListener,
        CustomerFoodItemFragment.OnListFragmentInteractionListener,
        CustomerOrderFragment.OnListFragmentInteractionListener,
        CustomerReceiptFragment.OnFragmentInteractionListener {

    // database and authentication instances
    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;
    private final FirebaseMessaging mMessaging = FirebaseMessaging.getInstance();

    private FirebaseUser user;
    private DatabaseReference dbRef;

    // typefaces
    private AssetManager assetManager;
    private Typeface typefaceRaleway;

    // current user's order
    //private Order order;
    private Cart cart;
    private ArrayList<Order> mOrders;

    // floating action button
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_activity_homepage);

        cart = new Cart();
        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Initialize fonts
        typefaceRaleway = Typeface.createFromAsset(getAssets(), "Raleway-Medium.ttf");

        initializeComponents();
    }

    private void initializeComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        FloatingActionButton orderButton = findViewById(R.id.submit_or_cancel_button);
        orderButton.hide();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        // set the display name
        TextView mUserName_editText = headerView.findViewById(R.id.userName_editText);
        mUserName_editText.setTypeface(typefaceRaleway);
        mUserName_editText.setText(user.getEmail());

        // Default item selected
        navigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        navigationView.setNavigationItemSelectedListener(this);
        subscribeToTopic();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // load past orders in the background without displaying
        retrievePastOrders(false);

        // displays the default view fragment
        setDefaultView();
    }

    /**
     * loads the default fragment into fragContainer if no fragment is specified.
     */
    private void setDefaultView() {
        Fragment fragment = HawkerCentreFragment.newInstance(1);
        String tag = getResources().getString(R.string.hawkerCentreFrag);
        displayFragment(fragment, tag);
    }

    /**
     * retrieves the most recent past orders corresponding to currently signed in customer
     */
    private void retrievePastOrders(final boolean display) {
        System.out.println("retrieving past orders");
        String uid = mAuth.getUid();
        mOrders = new ArrayList<>();
        System.out.println("UID: " + uid);
        mDb.getReference(getResources().getString(R.string.order_ref))
                .orderByChild("user_id")
                .equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mOrders.clear();
                        System.out.println("datasnapshot contains: " + dataSnapshot);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            System.out.println("snap: " + snapshot);
                            Order order = snapshot.getValue(Order.class);
                            mOrders.add(order);
                        }
                        if (display) {
                            Fragment fragment = PastOrdersFragment.newInstance(1, mOrders, "CUSTOMER");
                            String tag = getResources().getString(R.string.pastOrdersFragment);
                            displayFragment(fragment, tag);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("db", databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                super.onBackPressed();
            } else {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customer_homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String tag = null;

        if (id == R.id.receipts_view) {
            retrievePastOrders(true);
        } else if (id == R.id.settings_view) {
            clearViewsInContentView();
            android.app.Fragment profileFrag = ProfileFragment.newInstance();
            tag = getResources().getString(R.string.profileFrag);
            displayFragment(profileFrag, tag);
        } else if (id == R.id.nav_send) {   // sign out
            signout();
        } else {
            System.out.println("order view selected");
            fragment = HawkerCentreFragment.newInstance(1);
            tag = getResources().getString(R.string.hawkerCentreFrag);
        }

        if (fragment != null) {
            displayFragment(fragment, tag);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signout() {
        startActivity(new Intent(CustomerHomepage.this, LoginActivity.class));
        finish();
        mAuth.signOut();
    }

    /**
     * displays the customer receipt fragment on click of past order.
     */
    public void displayCustomerReceiptFragment(Order orderClicked) {
        Fragment newFrag = CustomerReceiptFragment.newInstance(orderClicked, retrieveCustomerAddress());
        displayFragment(newFrag, "customerReceiptFragment");
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

    private void displayFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag_container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    private void displayFragment(android.app.Fragment fragment, String tag) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frag_container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    public void clearViewsInContentView() {
        ((ConstraintLayout) findViewById(R.id.frag_container)).removeAllViews();
    }

    /**
     * onclick callback when viewholder containing hawkerCentre is clicked.
     * Display all hawker stalls at this hawker centre.
     *
     * @param hawkerCentre hawkerCentre object associated with viewholder clicked.
     */
    @Override
    public void onListFragmentInteraction(HawkerCentre hawkerCentre) {
        // retrieve the fragment containing a recyclerview of hawkerStalls
        CustomerHawkerStallFragment hsFragment = CustomerHawkerStallFragment.newInstance(1, hawkerCentre);

        // display fragment
        displayFragment(hsFragment, getResources().getString(R.string.hawkerStallFrag));
    }

    // onclick callback when hawkerStall item is clicked.
    // Display menu items associated with hawkerStall.
    public void onListFragmentInteraction(HawkerStall hawkerStall) {
        CustomerFoodItemFragment fragment = CustomerFoodItemFragment.newInstance(1, hawkerStall);

        // display fragment
        displayFragment(fragment, getResources().getString(R.string.foodItemFrag));
    }

    @Override
    public void onListFragmentInteraction(FoodItem item, int qty) {
        if (!cart.hasOrder()) {    // if no orders in cart yet
            initializeCart();
        }

        // add order to fragment and notify adapter of the update
        cart.getContents().addOrder(new OrderItem(item, qty));
        Toast.makeText(this, "added order to cart!", Toast.LENGTH_SHORT)
                .show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fab.setTooltipText(getResources().getString(R.string.fab_tooltip));
        }
        fab.show();
    }

    @SuppressLint("NewApi")
    private void initializeCart() {
        HawkerStall hs = CustomerFoodItemFragment.getHawkerStall();

        cart.makeStartingOrder(new Order(null, user.getUid(), hs.getHc_id()));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerOrderFragment customerOrderFragment = CustomerOrderFragment.newInstance(1, cart);
                // display fragment
                displayFragment(customerOrderFragment, CustomerHomepage.this.getResources().getString(R.string.orderFrag));
            }
        });
        fab.setTooltipText("View Cart");
        fab.show();    // view cart button
    }

    // saves message to db under notificationRequests/:uid/:pushid/notification
    private void sendMessage(String target_uid, String title, String body) {
        DatabaseReference dbRef = mDb
                .getReference(getString(R.string.notificationRequests))
                .child(target_uid);

        Map notification = new HashMap<>();
        notification.put(getString(R.string.user_id), target_uid);
        notification.put(getString(R.string.message_title), title);
        notification.put(getString(R.string.message_body), body);

        dbRef.push().setValue(notification);
    }

    private void subscribeToTopic() {
        /* use a topic that uniquely identifies user to ensure we get all messages for this user. */
        mMessaging.subscribeToTopic(mAuth.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("topic subscription", "Successfully subscribed to " + mAuth.getUid());
                        } else {
                            Log.e("topic subscription", task.getException().getMessage());
                        }
                    }
                });
    }

    private void unsubscribeFromTopic() {
        mMessaging.unsubscribeFromTopic(mAuth.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("topic subscription", "Successfully unsubscribed from " + mAuth.getUid());
                        } else {
                            Log.e("topic subscription", task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void onListFragmentInteraction(OrderItem item) {
    }

    public Order getOrder() {
        return cart.getContents();
    }

    public FirebaseUser getUser() {
        return user;
    }


    @Override
    public void onFragmentInteraction(Order order) {
        /**
         * commented out code is a duplicate of Courier.onFragmentInteraction(Order order, Overloader boolean)
         * order will be marked as complete once courier successfully scans qr code on customer's receipt
         * using the mtd call Courier.onFragInteraction(..)
         */
        //        mDb.getReference("Orders")
//                .child(order.getTimestamp())
//                .child("transaction_complete")
//                .setValue(true)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(CustomerHomepage.this, "Transaction marked as complete!", Toast.LENGTH_LONG).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(CustomerHomepage.this, "Failed to mark as complete, try again later.", Toast.LENGTH_LONG).show();
//                    }
//                });
    }

}
