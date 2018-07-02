package com.eatlah.eatlah.activities.Customer;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.Customer.CustomerReceiptFragment;
import com.eatlah.eatlah.fragments.General.PastOrdersFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerFoodItemFragment;
import com.eatlah.eatlah.fragments.Customer.HawkerCentreFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerHawkerStallFragment;
import com.eatlah.eatlah.fragments.Customer.CustomerOrderFragment;
import com.eatlah.eatlah.models.FoodItem;
import com.eatlah.eatlah.models.HawkerCentre;
import com.eatlah.eatlah.models.HawkerStall;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

    private FirebaseUser user;
    private DatabaseReference dbRef;

    // typefaces
    private AssetManager assetManager;
    private Typeface typefaceRaleway;

    // current user's order
    private Order order;
    private ArrayList<Order> mOrders;

    // floating action button
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_activity_homepage);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrievePastOrders();
    }

    /**
     * retrieves the most recent past orders corresponding to currently signed in customer
     */
    private void retrievePastOrders() {
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
            super.onBackPressed();
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
            if (mOrders != null) {
                fragment = PastOrdersFragment.newInstance(1, mOrders, PastOrdersFragment.CUSTOMER);
                tag = getResources().getString(R.string.pastOrdersFragment);
            }
        } else if (id == R.id.settings_view) {
            // todo view for user settings
            // includes password reset

        } else if (id == R.id.nav_send) {
            // todo cloud messaging
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
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frag_container, fragment, tag);
        ft.addToBackStack(getTitle().toString());
        ft.commit();
        System.out.println("replaced fragment and committed");
    }

    /**
     * onclick callback when viewholder containing hawkerCentre is clicked.
     * Display all hawker stalls at this hawker centre.
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
        if (order == null) {    // if no orders in cart yet
           initializeCart();
        }

        // add order to fragment and notify adapter of the update
        order.addOrder(new OrderItem(item, qty));
        Toast.makeText(this, "added order to cart!", Toast.LENGTH_SHORT)
                .show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fab.setTooltipText(getResources().getString(R.string.fab_tooltip));
        }
        fab.show();
    }

    private void initializeCart() {
        HawkerStall hs = CustomerFoodItemFragment.getHawkerStall();
        dbRef = mDb
                .getReference(getResources().getString(R.string.order_ref))
                .push();
        String timestamp = dbRef.getKey();
        order = new Order(timestamp, user.getUid(), hs.getHc_id());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save order to db
                dbRef.setValue(order)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("db", "successfully saved order");
                                    CustomerOrderFragment customerOrderFragment = CustomerOrderFragment.newInstance(1, order);

                                    // display fragment
                                    displayFragment(customerOrderFragment, CustomerHomepage.this.getResources().getString(R.string.orderFrag));
                                } else {
                                    Log.e("db", task.getException().getMessage());
                                }
                            }
                        });
            }
        });
        fab.show();
    }
    
    @Override
    public void onListFragmentInteraction(OrderItem item) {
    }

    public Order getOrder() {
        return this.order;
    }
    public FirebaseUser getUser() { return user; }


    @Override
    public void onFragmentInteraction(Order order) {
        mDb .getReference("Orders")
            .child(order.getTimestamp())
            .child("transaction_complete")
            .setValue(true)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(CustomerHomepage.this, "Transaction marked as complete!", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CustomerHomepage.this, "Failed to mark as complete, try again later.", Toast.LENGTH_LONG).show();
                }
            });
    }
}
