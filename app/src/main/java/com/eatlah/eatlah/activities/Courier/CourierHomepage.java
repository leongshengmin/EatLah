package com.eatlah.eatlah.activities.Courier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.General.LoginActivity;
import com.eatlah.eatlah.fragments.Courier.CourierOrderItemsFragment;
import com.eatlah.eatlah.fragments.Courier.CourierPendingOrderFragment;
import com.eatlah.eatlah.fragments.Courier.CourierReceiptFragment;
import com.eatlah.eatlah.fragments.General.PastOrdersFragment;
import com.eatlah.eatlah.fragments.General.ProfileFragment;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CourierHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CourierPendingOrderFragment.OnListFragmentInteractionListener,
        CourierOrderItemsFragment.OnListFragmentInteractionListener,
        CourierReceiptFragment.OnFragmentInteractionListener {

    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mDb;
    private final FirebaseMessaging mMessaging;

    private ArrayList<Order> orders;

    public CourierHomepage() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance("https://eatlah-fe598.firebaseio.com/");
        mMessaging = FirebaseMessaging.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courier_activity_homepage);

        retrieveExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        hideFab();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        setUserDisplayInfo(navigationView.getHeaderView(0));
        navigationView.setNavigationItemSelectedListener(this);
        setDefaultView();

        subscribeToTopic();
    }

    /**
     * if navigation drawer is not used or a view is not selected,
     * the default view will be a list of all the pending orders.
     */
    @SuppressLint("NewApi")
    private void setDefaultView() {
        Fragment fragment = CourierPendingOrderFragment.newInstance(1);
        String tag = getResources().getString(R.string.courierPendingOrderFrag);
        displayFragment(fragment, tag);
    }

    private void disableAttendToOrderButton() {
        Button attendToOrder_btn = findViewById(R.id.attendToOrderButton);
        System.out.println("disabling attend to order button " + attendToOrder_btn);
        if (attendToOrder_btn != null) {
            attendToOrder_btn.setVisibility(View.INVISIBLE);
            attendToOrder_btn.setClickable(false);
        }
    }

    private void retrieveExtras() {
        String customerAddress = getIntent().getStringExtra(getString(R.string.customer_address));
        Order order = (Order) getIntent().getSerializableExtra(getString(R.string.order_ref));
        System.out.println("order : " + order + "customer Address: " + customerAddress);

        endOfMapsActivity(order, customerAddress);
    }

    /**
     * responds to the end of maps activity by displaying the receipt of the order associated if any.
     * @param order
     * @param customerAddress
     */
    private void endOfMapsActivity(Order order, String customerAddress) {
        boolean displayReceipt = getIntent().getBooleanExtra(getResources().getString(R.string.toDisplayReceiptView), false);

        if (displayReceipt) {
            displayCourierReceipt(order, customerAddress);
        }
    }

    public void displayCourierReceipt(Order order, String customerAddress) {
        // create foodItems fragment and pass in Order containing list of foodItems as arg
        Fragment fragment = CourierReceiptFragment.newInstance(order, customerAddress);

        // display the fragment
        displayFragment(fragment, getResources().getString(R.string.courier_receipt_fragment));
    }

    private void displayFragment(android.app.Fragment fragment, String tag) {
        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frag_container, fragment, tag);
        ft.addToBackStack(tag);
        ft.commit();
    }

    public void clearViewsInContentView() {
        ((ConstraintLayout) findViewById(R.id.frag_container)).removeAllViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Fragment ft = getSupportFragmentManager().findFragmentByTag(getString(R.string.courier_receipt_fragment));
        if (ft==null) {
            setDefaultView();
        } else {
          getSupportFragmentManager().beginTransaction().show(ft).commit();
        }
    }

    // saves message to db under notificationRequests/:uid/:pushid/notification
    public void sendMessage(String target_uid, String title, String body, boolean is_background, String timestamp) {
        Log.d("fcm", "attempting to send message to " + target_uid + " with title: " + title + " and body: " + body);
        DatabaseReference dbRef = mDb
                .getReference(getString(R.string.notificationRequests));

        Map notification = new HashMap<>();
        notification.put(getString(R.string.user_id), target_uid);
        notification.put(getString(R.string.message_title), title);
        notification.put(getString(R.string.message_body), body);
        notification.put(getString(R.string.is_background), is_background);
        notification.put(getString(R.string.timestamp), timestamp);

        dbRef.push().setValue(notification)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("fcm", "message successfully sent");
                        } else {
                            Log.e("fcm", task.getException().getMessage());
                        }
                    }
                });
    }

    public void sendMessage(Order order, String title, String body) {
        Date date = new Date();
        long time = date.getTime();
        Timestamp timestamp = new Timestamp(time);

        // sends message to customer
        sendMessage(order.getUser_id(),
                String.format("\"%s\"", title),
                String.format("\"%s\"", body),
                false,
                String.format("\"%s\"", timestamp.toString()));
    }

    public void subscribeToTopic() {
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

    public void unsubscribeFromTopic() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager()
                .findFragmentByTag(getString(R.string.profileFrag));
        profileFragment.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * retrieves the most recent past orders corresponding to currently signed in courier
     * and displays pastOrders fragment
     */
    private void retrievePastOrders() {
        System.out.println("retrieving past orders");
        String uid = mAuth.getUid();
        orders = new ArrayList<>();
        System.out.println("UID: " + uid);
        mDb.getReference(getResources().getString(R.string.order_ref))
                .orderByChild("courier_id")
                .equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println("datasnapshot contains: " + dataSnapshot);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            System.out.println("snap: " + snapshot);
                            Order order = snapshot.getValue(Order.class);
                            orders.add(order);
                        }
                        Fragment fragment = PastOrdersFragment.newInstance(1, orders, PastOrdersFragment.COURIER);
                        String tag = getResources().getString(R.string.pastOrdersFragment);
                        displayFragment(fragment, tag);
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
            disableAttendToOrderButton();
            System.out.println("backstack contains " + count + " items");
            if (count > 0) {
                System.out.println("POPPING fragment backstack");
                getFragmentManager().popBackStack();
            } else {
                System.out.println("super on back pressed called");
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.courier_homepage, menu);
        return true;
    }

    /**
     * sets the header of nav view to user's email
     */
    private void setUserDisplayInfo(View view) {
        TextView textView = view.findViewById(R.id.header_text);
        textView.setText(mAuth.getCurrentUser().getEmail());
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
        System.out.println("item selected: " + id);

        String tag = null;
        if (id == R.id.nav_camera) {
            Fragment fragment = CourierPendingOrderFragment.newInstance(1);
            tag = getResources().getString(R.string.courierPendingOrderFrag);
            displayFragment(fragment, tag);
        } else if (id == R.id.nav_manage) {   // profile
            android.app.Fragment fragment = ProfileFragment.newInstance();
            tag = getResources().getString(R.string.profileFrag);
            displayFragment(fragment, tag);
        } else if (id == R.id.nav_share) {  // view past orders
            retrievePastOrders();
        } else if (id == R.id.nav_send) {   // sign out
            signout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signout() {
        startActivity(new Intent(CourierHomepage.this, LoginActivity.class));
        finish();
        mAuth.signOut();
    }

    private void displayFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag_container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

    private void hideFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setClickable(false);
    }

    @Override
    /**
     * callback for CourierPendingOrdersFragment
     */
    public void onListFragmentInteraction(Order item) {
        hideFab();
        // create foodItems fragment and pass in Order containing list of foodItems as arg
        Bundle bd = new Bundle();
        bd.putSerializable(getResources().getString(R.string.order_ref), item);
        Fragment fragment = CourierOrderItemsFragment
                .newInstance(1);
        fragment.setArguments(bd);

        // display the fragment
        displayFragment(fragment, getResources().getString(R.string.courierFoodItemsFrag));
    }

    @Override
    /**
     * callback for CourierOrderItemsFragment
     */
    public void onListFragmentInteraction(OrderItem item) {
        // null
    }

    @Override
    /**
     * callback for CourierReceiptFragment
     */
    public void onFragmentInteraction(Order order, Boolean overloader) {
        // Set complete
        mDb.getReference("Orders")
           .child(order.getTimestamp())
           .child("transaction_complete")
           .setValue(true)
           .addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Snackbar.make(findViewById(R.id.frag_container), getResources().getString(R.string.completedOrder), Snackbar.LENGTH_LONG)
                           .show();
                   redirectToHome();
               }

               private void redirectToHome() {
                   setDefaultView();
               }
           })
           .addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Toast.makeText(CourierHomepage.this, "Failed: " + e, Toast.LENGTH_SHORT).show();
               }});
    }
}
