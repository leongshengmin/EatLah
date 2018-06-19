package com.eatlah.eatlah.activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.hawker.AcceptedOrderFragment;
import com.eatlah.eatlah.fragments.hawker.AcceptedOrderItemFragment;
import com.eatlah.eatlah.fragments.hawker.MenuItemFragment;
import com.eatlah.eatlah.fragments.hawker.ModifyMenuItemFragment;
import com.eatlah.eatlah.models.FoodItem;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.eatlah.eatlah.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HawkerHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AcceptedOrderFragment.OnListFragmentInteractionListener,
        AcceptedOrderItemFragment.OnListFragmentInteractionListener,
        MenuItemFragment.OnListFragmentInteractionListener,
        ModifyMenuItemFragment.OnFragmentInteractionListener {

    // database and authentication instances
    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

    // floating action button
    private FloatingActionButton fab;
    private NavigationView navigationView;

    // Fields
    public static User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hawker_activity_homepage);

        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mDb.getReference("users")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User u = dataSnapshot.getValue(User.class);
                        saveUser(u);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

        initializeComponents();
    }

    private void saveUser(User _user) {
        mUser = _user;
    }

    private void initializeComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.hawker_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.hawker_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add new item selected.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                displayFragment(ModifyMenuItemFragment.newInstance(getResources().getString(R.string.NEW_ITEM)), "ModifyMenuItemFragment");
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hawker_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.hawker_nav_view);
        View headerView = navigationView.getHeaderView(0);
        // set the display name
        TextView mHawkerName_editText = (TextView) headerView.findViewById(R.id.hawkerName_textView);
        mHawkerName_editText.setText(user.getEmail());

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(2).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(2)); // Default select admin

        setActionBarTitle("Admin Page");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hawker_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        // If it's the admin page, show the FAB. else hide it. Change the art on fab.
        System.out.println("Selected page: " + navigationView.getMenu().getItem(2).isChecked());
        if (navigationView.getMenu().getItem(2).isChecked()) {
            showFab(); plusFab();
        }
        if (navigationView.getMenu().getItem(1).isChecked() || navigationView.getMenu().getItem(0).isChecked()) {
            hideFab(); sendFab();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hawker_homepage, menu);
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
        System.out.println("Item chosen." + id);

        Fragment fragment = null;
        String tag = null;

        if (id == R.id.nav_accepted_orders) {
            fragment = AcceptedOrderFragment.newInstance(1);
            setActionBarTitle("Accepted Orders");
            hideFab();
            sendFab();
            tag = "AcceptedOrderFragment";
        } else if (id == R.id.nav_completed_orders) {
            setActionBarTitle("Completed Orders");
            hideFab();
            sendFab();
        } else if (id == R.id.nav_admin_page) {
            fragment = MenuItemFragment.newInstance(1);
            setActionBarTitle("Admin Page");
            showFab();
            plusFab();
            tag = "MenuItemFragment";
        } else if (id == R.id.nav_hawker_send) {

        }

        if (fragment != null) {
            displayFragment(fragment, tag);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hawker_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.hawker_frag_container, fragment, tag);
        ft.addToBackStack(getTitle().toString());
        ft.commit();
        System.out.println("replaced fragment and committed");

    }

    /**
     * Use to make FAB for use of the accepted/completed orders page
     */
    public void sendFab() {
        fab.setImageResource(R.drawable.ic_send_black_24dp);
    }

    /**
     * Use to make FAB for use of the Admin Page.
     */
    public void plusFab() {
        fab.setImageResource(R.drawable.ic_add_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add new item selected.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                displayFragment(ModifyMenuItemFragment.newInstance(getResources().getString(R.string.NEW_ITEM)), "ModifyMenuItemFragment");
            }
        });
    }
    public void hideFab() {
        fab.setVisibility(FloatingActionButton.INVISIBLE);
    }
    public void showFab() {
        fab.setVisibility(FloatingActionButton.VISIBLE);
    }

    // When order is clicked
    @Override
    public void onListFragmentInteraction(Order order) { // For orders pages
        AcceptedOrderItemFragment fragment = AcceptedOrderItemFragment.newInstance(1, order);
        displayFragment(fragment, getResources().getString(R.string.acceptedOrderItemFrag));
        showFab();
    }


    @Override
    public void onListFragmentInteraction(OrderItem item) {

    }

    @Override
    public void onListFragmentInteraction(FoodItem item) { // For menu items of admin page
        ModifyMenuItemFragment fragment = ModifyMenuItemFragment.newInstance(item.get_id());
        displayFragment(fragment, getResources().getString(R.string.modifyMenuItemFrag));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    public void setActionBarTitle(String actionBarTitle) {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(actionBarTitle);
    }
}
