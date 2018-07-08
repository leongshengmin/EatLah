package com.eatlah.eatlah.activities.Courier;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eatlah.eatlah.BuildConfig;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.Courier.CourierOrderItemsDialogFragment;
import com.eatlah.eatlah.fragments.Courier.CourierOrderItemsFragment;
import com.eatlah.eatlah.helpers.Courier.FetchDirectionsFromService;
import com.eatlah.eatlah.helpers.Courier.OnTaskCompletedListener;
import com.eatlah.eatlah.helpers.Courier.fetchLatLongFromService;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
import com.eatlah.eatlah.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourierMapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, CourierOrderItemsFragment.OnListFragmentInteractionListener,
        OnTaskCompletedListener {

    // bunch of location related apis
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private GoogleMap mMap;
    private final FirebaseDatabase mDb = FirebaseDatabase.getInstance();
    private CameraUpdate cu;

    // permissions
    private long UPDATE_INTERVAL = 60000; // 30s
    private long FASTEST_INTERVAL = 30000;  // 60s

    private HashMap<String, Order> orderDict;
    private Order attendingOrder;
    private String customerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courier_activity_maps);
        orderDict = new HashMap<>();

        startLocationUpdates();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        loadMap();
    }


    /**
     * sets bounding box around sg
     */
    private LatLngBounds getBoundingBox() {
        LatLng ne = new LatLng(1.478400, 104.094500);
        LatLng sw = new LatLng(1.149600, 103.594000);
        return new LatLngBounds(sw, ne);
    }

    private void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * retrieves pending orders and pins the locations onto map.
     */
    private void mapOrders() {
        System.out.println("mapping orders");
        // retrieve the pending orders
        ArrayList<Order> pendingOrders = (ArrayList<Order>) getIntent()
                .getSerializableExtra(getResources().getString(R.string.pendingOrders));

        // display the orders
        for (Order order : pendingOrders) {
            orderDict.put(order.getHawkerCentre_id(), order);
            getLatLongFromPlace(order.getHawkerCentre_id());
            System.out.println("retrieved pending order: " + order);
        }
    }

    /**
     * retrieves lat lng from queried place and pins latlng object onto map.
     */
    private void getLatLongFromPlace(String postalCode) {
        new fetchLatLongFromService(postalCode, orderDict, mMap)
                .execute();
    }

    /**
     * pins the given latlng object onto map
     * @param latLng
     * @param markerTag postal code of location
     * @param title heading of marker (e.g. currentLocation, pickupLocation,...)
     * @param snippet body of marker
     */
    private void pinOnToMap(LatLng latLng, String markerTag, String title, String snippet) {
        System.out.println("pinning " + markerTag + " onto map");
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        marker.setTag(markerTag);
        marker.setSnippet(snippet);
    }

    /**
     * Update the UI displaying the location data
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null && mMap != null) {
            System.out.println("current location found!");

            // add pin at current location
            LatLng currentLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            pinOnToMap(currentLoc, getResources().getString(R.string.currentLocation), getResources().getString(R.string.currentLocation), getResources().getString(R.string.currentLocation));
        }
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested.
     *
     * Retrieves current location of user.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // create LocationSettingsRequest obj using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // check if location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                System.out.println("current location: " + locationResult.getLastLocation().toString());
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    /**
     * if current location of user changes, mapView is updated to reflect change in location.
     * @param location
     */
    public void onLocationChanged(Location location) {
        // new location has been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;
        updateLocationUI();
    }

    /**
     * stops updating location
     */
    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * requests for permission to use location.
     * If permission is refused in settings, open up settings view.
     * Else, connect to map and retrieve current location.
     */
    public void requestConnectionToLocationServices() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mMap.setMyLocationEnabled(true);
                        updateLocationUI();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Open device settings activity when the permission is denied permanently.
     */
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume location updates if permission granted
        if (checkPermissions()) {
            startLocationUpdates();
        }

    }

    /**
     * checks for permission to access location services from user.
     * @return true if permission is granted, false otherwise.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        cu = CameraUpdateFactory.newLatLngBounds(getBoundingBox(), 2000, 50000, 5);

        requestConnectionToLocationServices();

        // retrieve list of pending orders to update mapview
        mapOrders();
        mMap.animateCamera(cu);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String postalCode = (String) marker.getTag();
        System.out.println("marker clicked: " + postalCode);

        Order order = orderDict.get(postalCode);
        if (order == null) {    // marker is not meant to be clickable (only ORDER markers are clickable)
            System.out.println("order not found for " + postalCode);
            return false;
        }
        displayPendingOrderInfo(order, marker);
        return false;
    }

    /**
     * given 2 markers, a and b, returns the distance between the 2 markers in floating point precision.
     * @param a
     * @param b
     * @return
     */
    private float getDistanceBetween(Marker a, Marker b) {
        LatLng latLng_a = a.getPosition();
        Location loc_a = new Location("a");
        loc_a.setLatitude(latLng_a.latitude);
        loc_a.setLongitude(latLng_a.longitude);

        LatLng latLng_b = b.getPosition();
        Location loc_b = new Location("b");
        loc_b.setLatitude(latLng_b.latitude);
        loc_b.setLongitude(latLng_b.longitude);

        return loc_a.distanceTo(loc_b) / 1000;
    }

    private float getDistanceFrom(Marker other) {
        if(mCurrentLocation == null) return 0;

        LatLng latLng_a = other.getPosition();
        Location loc_a = new Location("a");
        loc_a.setLatitude(latLng_a.latitude);
        loc_a.setLongitude(latLng_a.longitude);

        return loc_a.distanceTo(mCurrentLocation) / 1000;
    }

    /**
     * display pending order information
     * @param order
     * @param marker
     */
    private void displayPendingOrderInfo(final Order order, Marker marker) {
        System.out.println("order: " + order);
        displayOrder(order, marker);
        TextView dir_textView = findViewById(R.id.directions_textView);
        dir_textView.setMovementMethod(new ScrollingMovementMethod());

        setDistanceFromCurrent(getDistanceFrom(marker), dir_textView);

        final Button viewOrders_button = findViewById(R.id.viewOrders_button);
        configureViewOrdersButton(order, viewOrders_button);

        final Button attendToOrder_button = findViewById(R.id.attendToOrder_button);
        if (order.getCourier_id() == null || order.getCourier_id().isEmpty()) { // if no courier attending to this order yet
            configureAttendToOrderButton(order, attendToOrder_button, viewOrders_button, marker);
        }
    }

    private void setCollectionTime(final Order order, final TextView time_textView) {
        System.out.println("setting collection time:" + order + " timeview: " + time_textView);
        String time = String.format("Collection Time: %s", order.getCollectionTime());
        time_textView.setText(time);
    }

    private void setDistanceFromCurrent(final float dist, final TextView distance_textView) {
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.DOWN);
        String distance = String.format("Distance from Current: %s", df.format(dist));
        distance_textView.setText(distance);
    }

    /**
     * configures view order items button and sets on click listener.
     * @param order
     * @param viewOrders_button
     */
    private void configureViewOrdersButton(final Order order, final Button viewOrders_button) {
        viewOrders_button.setVisibility(View.VISIBLE);
        viewOrders_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create foodItems fragment and pass in Order containing list of foodItems as arg
                Bundle bd = new Bundle();
                bd.putSerializable(getResources().getString(R.string.order_ref), order);
                CourierOrderItemsDialogFragment fragment = new CourierOrderItemsDialogFragment();
                fragment.setArguments(bd);

                // display the fragment
                displayFragment(fragment, getResources().getString(R.string.courierOrderItemsDialogFrag));
            }

            private void displayFragment(CourierOrderItemsDialogFragment fragment, String tag) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                fragment.show(ft, tag);
                ft.addToBackStack(fragment.getTag());
            }
        });
    }

    /**
     * initializes the attend to order button and sets on click listener
     * @param order
         * @param attendToOrder_button
     */
    private void configureAttendToOrderButton(final Order order, final Button attendToOrder_button, final Button viewOrders_button, final Marker marker) {
        System.out.println("marker: " + marker.getTag());
        attendToOrder_button.setVisibility(View.VISIBLE);
        attendToOrder_button.setOnClickListener(new View.OnClickListener() {
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();

            @Override
            public void onClick(View v) {
                attendingOrder = order;

                // update order fields in db
                // to remove order from global courier view
                updateOrder();

                // map out route from current location to pickup location
                retrieveRouteToPickup();

                // get customer address from customerUID for routing later
                retrieveCustomerAddress();

                disableAttendToOrderButton();

                Toast.makeText(CourierMapsActivity.this, "Loading directions...", Toast.LENGTH_LONG)
                        .show();
            }

            /**
             * retrieves route from current location to pickup location async
             */
            private void retrieveRouteToPickup() {
                System.out.println("Retrieving route to pickup");
                new FetchDirectionsFromService(mCurrentLocation, (String) marker.getTag(), mMap, CourierMapsActivity.this)
                        .execute();
            }

            /**
             * updates the order in db
             * since CourierPendingOrderFrag is listening to real-time value changes to orders in db
             * UI will be updated automatically.
             */
            private void updateOrder() {
                order.setCourier_id(mAuth.getUid());
                mDb.getReference(getResources().getString(R.string.order_ref))
                        .child(order.getTimestamp())
                        .setValue(order);
            }

            /**
             * retrieves customer address from customerUID async
             */
            private void retrieveCustomerAddress() {
                System.out.println(order.getUser_id());
                mDb.getReference(getResources().getString(R.string.user_ref))
                        .child(order.getUser_id())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                System.out.println("datasnapshot: " + dataSnapshot);
                                User customer = dataSnapshot.getValue(User.class);
                                customerAddress = customer.getAddress();
                                System.out.println("customer address retrieved: " + customerAddress);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("db", databaseError.getMessage());
                            }
                        });
            }
        });
    }

    /**
     * clears map and starts new route to customer's address
     * if courier has collected all orders.
     */
    private void fetchNewRoute() {
        if (customerAddress != null) {
            System.out.println("fetching new route");
            mMap.clear();
            new FetchDirectionsFromService(mCurrentLocation, customerAddress, mMap, CourierMapsActivity.this)
                    .execute();
        }
    }

    /**
     * displays confirmed order in view
     * @param order
     * @param marker
     */
    private void displayOrder(Order order, Marker marker) {
        TextView addr_textView = findViewById(R.id.address_textView);
        TextView time_textView = findViewById(R.id.time_textView);
        addr_textView.setText(marker.getSnippet());
        setCollectionTime(order, time_textView);
    }

    /**
     * listener notified when all orderItems are collected
     * @param item
     */
    @Override
    public void onListFragmentInteraction(OrderItem item) {
        System.out.println("all items collected, fetching new route");
        fetchNewRoute();
    }

    @Override
    /**
     * callback when FetchDirectionsFromService is completed.
     */
    public void onTaskCompleted(List<String> directions) {
        System.out.println("retrieved DIRECTIONS!");
        configEndRouteButton();
        displayDirections(directions);

    }

    private void displayDirections(List<String> directions) {
        TextView directions_textView = findViewById(R.id.directions_textView);
        directions_textView.setMovementMethod(new ScrollingMovementMethod());

        directions_textView.setVisibility(View.VISIBLE);
        System.out.println("size of directions:" + directions.size());
        StringBuilder sb = new StringBuilder();
        for (String line : directions) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }
        System.out.println(sb.toString());
        directions_textView.setText(sb.toString());
    }

    /**
     * reuses attendToOrder button which was set to invisible
     * for courier to end navigation upon order completion.
     */
    private void configEndRouteButton() {
        Button endRoute_button = findViewById(R.id.endRoute_btn);
        endRoute_button.setText(getResources().getString(R.string.endRouteLabel));
        endRoute_button.setVisibility(View.VISIBLE);
        endRoute_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourierMapsActivity.this, CourierHomepage.class);
                intent.putExtra(getResources().getString(R.string.order_ref), attendingOrder);
                intent.putExtra(getResources().getString(R.string.customer_address), customerAddress);
                startActivity(intent);
                finish();
            }
        });
    }

    private void disableAttendToOrderButton() {
        Button attendToOrder_button = findViewById(R.id.attendToOrder_button);
        attendToOrder_button.setVisibility(View.INVISIBLE);
        attendToOrder_button.setClickable(false);
    }

    @Override
    public void onTaskCompleted(String address) {
        // null
    }
}
