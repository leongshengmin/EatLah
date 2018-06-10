package com.eatlah.eatlah.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.eatlah.eatlah.BuildConfig;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.fragments.CourierOrderItemsDialogFragment;
import com.eatlah.eatlah.fragments.CourierOrderItemsFragment;
import com.eatlah.eatlah.fragments.CourierPendingOrderFragment;
import com.eatlah.eatlah.models.Order;
import com.eatlah.eatlah.models.OrderItem;
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
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CourierMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, CourierOrderItemsFragment.OnListFragmentInteractionListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courier_maps_activity);
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
        new fetchLatLongFromService(postalCode)
                .execute();
    }

    /**
     * pins the given latlng object onto map
     */
    private void pinOnToMap(LatLng latLng, String markerTag, String title) {
        System.out.println("pinning " + markerTag + " onto map");
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        marker.setTag(markerTag);
    }

    /**
     * Update the UI displaying the location data
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null && mMap != null) {
            System.out.println("current location found!");

            // add pin at current location
            LatLng currentLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            pinOnToMap(currentLoc, getResources().getString(R.string.currentLocation), getResources().getString(R.string.currentLocation));
        }
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
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
        if (marker.getTag().equals(getResources().getString(R.string.currentLocation))) return false;
        String postalCode = (String) marker.getTag();
        Order order = orderDict.get(postalCode);
        float dist = getDistanceFrom(marker);
        displayPopup(order, marker.getTitle(), dist);

//        setActivityResult(order);
//        System.out.println("marker clicked!");
//        finish();
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
     * popup showing more info about order
     * @param order
     */
    private void displayPopup(final Order order, String label, float dist) {
        final View popUpView = getLayoutInflater().inflate(R.layout.courier_order_markeronclick_layout, null, false);
        final PopupWindow mPopup = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true); // Creation of popup

        // bind item with popupView
        ((TextView) popUpView.findViewById(R.id.address_textView)).setText(label);
        ((TextView) popUpView.findViewById(R.id.deliveryTime_textView)).setText(order.getCollectionTime());
        ((TextView) popUpView.findViewById(R.id.distance_textView)).setText(Float.toString(dist)+" km");
        ((Button) popUpView.findViewById(R.id.viewOrders_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create foodItems fragment and pass in Order containing list of foodItems as arg
                Bundle bd = new Bundle();
                bd.putSerializable(getResources().getString(R.string.order_ref), order);
                CourierOrderItemsDialogFragment fragment = new CourierOrderItemsDialogFragment();
                fragment.setArguments(bd);

                // display the fragment
                displayFragment(fragment, "dialogFrag");
            }

            private void displayFragment(CourierOrderItemsDialogFragment fragment, String tag) {
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                fragment.show(ft, "dialog");
            }
        });

        mPopup.showAtLocation(popUpView, Gravity.CENTER, 0, 0); // Displaying popup
    }

    private void setActivityResult(Order order) {
        Intent intent = new Intent();
        Bundle bd = new Bundle();
        bd.putSerializable(getResources().getString(R.string.order_ref), order);

        intent.putExtra(getResources().getString(R.string.order_ref), bd);
        setResult(CourierPendingOrderFragment.MARKER_CLICKED_CODE, intent);
    }

    @Override
    public void onListFragmentInteraction(OrderItem item) {

    }

    public class fetchLatLongFromService extends
            AsyncTask<Void, Void, StringBuilder> {
        String place;


        public fetchLatLongFromService(String place) {
            super();
            this.place = place;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            this.cancel(true);
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            try {
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();

                String mapUrl = "https://geocode.xyz/locate=" + this.place + "?geoit=JSON" + "&region=SG";

                URL url = new URL(mapUrl);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(
                        conn.getInputStream());
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                String a = "";
                return jsonResults;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                JSONObject std_jsonObj = jsonObj.getJSONObject("standard");

                String addresst = std_jsonObj
                        .getString("addresst");
                String city = std_jsonObj.getString("city");
                String postal = std_jsonObj
                        .getString("postal").split("=")[1];

                String lat_helper = jsonObj.getString("latt");
                double lat = Double.valueOf(lat_helper);


                String lng_helper = jsonObj.getString("longt");
                double lng = Double.valueOf(lng_helper);

                LatLng point = new LatLng(lat, lng);
                String address = buildAddress(addresst, city, postal);
                pinOnToMap(point, place, address);

            } catch (JSONException e) {
                e.printStackTrace();

            }
        }

        private String buildAddress(String addresst, String city, String postal) {
            return String.format("%s, %s %s", addresst, city, postal);
        }
    }

}