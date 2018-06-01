package com.eatlah.eatlah.activities;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.widget.EditText;
import android.widget.Toast;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.FoodItemFragment;
import com.eatlah.eatlah.fragments.HawkerCentreFragment;
import com.eatlah.eatlah.fragments.HawkerStallFragment;
import com.eatlah.eatlah.fragments.OrderFragment;
import com.eatlah.eatlah.models.FoodItem;
import com.eatlah.eatlah.models.HawkerCentre;
import com.eatlah.eatlah.models.HawkerStall;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HawkerCentreFragment.OnListFragmentInteractionListener,
        HawkerStallFragment.OnListFragmentInteractionListener,
        FoodItemFragment.OnListFragmentInteractionListener,
        OrderFragment.OnListFragmentInteractionListener {

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

    // floating action button
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_homepage);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        // set the display name
        EditText mUserName_editText = (EditText) headerView.findViewById(R.id.userName_editText);
        mUserName_editText.setTypeface(typefaceRaleway);
        mUserName_editText.setText(user.getEmail());

        navigationView.setNavigationItemSelectedListener(this);
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

        if (id == R.id.receipts_view) {
            // todo view to display past orders ( receipts )

        } else if (id == R.id.settings_view) {
            // todo view for user settings
            // includes password reset

        } else if (id == R.id.nav_send) {
            // todo cloud messaging
        } else {
            // todo browse hawker centres fragment
            System.out.println("order view selected");
            fragment = HawkerCentreFragment.newInstance(1);
        }

        if (fragment != null) {
            displayFragment(fragment, getResources().getString(R.string.hawkerCentreFrag));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        HawkerStallFragment hsFragment = HawkerStallFragment.newInstance(1, hawkerCentre);

        // display fragment
        displayFragment(hsFragment, getResources().getString(R.string.hawkerStallFrag));
    }

    // onclick callback when hawkerStall item is clicked.
    // Display menu items associated with hawkerStall.
    public void onListFragmentInteraction(HawkerStall hawkerStall) {
        FoodItemFragment fragment = FoodItemFragment.newInstance(1, hawkerStall);

        // display fragment
        displayFragment(fragment, getResources().getString(R.string.foodItemFrag));
    }

    @Override
    public void onListFragmentInteraction(FoodItem item, int qty) {
        if (order == null) {    // if no orders in cart yet
           initializeCart();
        }
        order.addOrder(new OrderItem(item, qty));
        Toast.makeText(this, "added order to cart!", Toast.LENGTH_SHORT)
                .show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fab.setTooltipText(getResources().getString(R.string.fab_tooltip));
        }
        fab.show();
    }

    private void initializeCart() {
        HawkerStall hs = FoodItemFragment.getHawkerStall();
        dbRef = mDb.getReference(getResources().getString(R.string.order_ref));
        String timestamp = dbRef.push().getKey();
        order = new Order(timestamp, user.getProviderId(), hs.getHc_id());
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
                                    OrderFragment orderFragment = OrderFragment.newInstance(1, order);

                                    // display fragment
                                    displayFragment(orderFragment, CustomerHomepage.this.getResources().getString(R.string.orderFrag));
                                } else {
                                    Log.e("db", task.getException().getMessage());
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onListFragmentInteraction(OrderItem item) {

    }

    public Order getOrder() {
        return this.order;
    }
}
