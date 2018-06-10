package com.eatlah.eatlah.activities;

import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.CourierOrderItemsFragment;
import com.eatlah.eatlah.fragments.CourierPendingOrderFragment;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;

public class CourierHomepage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CourierPendingOrderFragment.OnListFragmentInteractionListener,
        CourierOrderItemsFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courier_activity_homepage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        hideFab();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
        getMenuInflater().inflate(R.menu.courier_homepage, menu);
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
        if (id == R.id.nav_camera) {
            fragment = CourierPendingOrderFragment.newInstance(1);
            tag = getResources().getString(R.string.courierPendingOrderFrag);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if (fragment != null) displayFragment(fragment, tag);

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

    private void hideFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
    }

    @Override
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
    public void onListFragmentInteraction(OrderItem item) {
        // null
    }
}