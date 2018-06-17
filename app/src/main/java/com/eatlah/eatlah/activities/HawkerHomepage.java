package com.eatlah.eatlah.activities;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
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
import com.eatlah.eatlah.fragments.AcceptedOrderFragment;
import com.eatlah.eatlah.fragments.AcceptedOrderItemFragment;
import com.eatlah.eatlah.fragments.MenuItemFragment;
import com.eatlah.eatlah.models.FoodItem;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HawkerHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AcceptedOrderFragment.OnListFragmentInteractionListener,
        AcceptedOrderItemFragment.OnListFragmentInteractionListener, MenuItemFragment.OnListFragmentInteractionListener {

    // database and authentication instances
    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

    // floating action button
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hawker_activity_homepage);

        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        initializeComponents();
    }

    private void initializeComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.hawker_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.hawker_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hawker_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.hawker_nav_view);
        View headerView = navigationView.getHeaderView(0);
        // set the display name
        TextView mHawkerName_editText = (TextView) headerView.findViewById(R.id.hawkerName_textView);
        mHawkerName_editText.setText(user.getEmail());

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.hawker_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
            tag = "AcceptedOrderFragment";
        } else if (id == R.id.nav_completed_orders) {

        } else if (id == R.id.nav_admin_page) {
            fragment = MenuItemFragment.newInstance(1);
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

    // When order is clicked
    @Override
    public void onListFragmentInteraction(Order order) {
        AcceptedOrderItemFragment fragment = AcceptedOrderItemFragment.newInstance(1, order);
        displayFragment(fragment, getResources().getString(R.string.acceptedOrderItemFrag));

    }


    @Override
    public void onListFragmentInteraction(OrderItem item) {

    }

    @Override
    public void onListFragmentInteraction(FoodItem item) {

    }
}
