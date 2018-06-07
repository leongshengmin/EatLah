package com.eatlah.eatlah.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.eatlah.eatlah.BuildConfig;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.Order;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CourierMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;

    // permissions
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private GoogleMap mMap;
    private FirebaseDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courier_maps_activity);
        mDb = FirebaseDatabase.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        loadMap();

        // initialize the necessary libraries
        init();

        // restore values from previous state
        restoreValuesFromBundle(savedInstanceState);

        checkPermissions();
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
        ArrayList<String> pendingOrders = (ArrayList<String>) getIntent()
                .getSerializableExtra(getResources().getString(R.string.pendingOrders));

        // display the orders
        for (String order : pendingOrders) {
          //  String postalCode = order.getHawkerCentre_id();
            getLatLongFromPlace(order);
            System.out.println("retrieved pending order: " + order);
        }
    }

    /**
     * retrieves lat lng from queried place and pins latlng object onto map.
     */
    private void getLatLongFromPlace(String postalCode) {
        try {
            Geocoder selected_place_geocoder = new Geocoder(this);
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(postalCode, 1);

            if (address == null) {
                Toast.makeText(this, "unable to find address", Toast.LENGTH_LONG)
                        .show();

            } else {
                Address location = address.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                pinOnToMap(latLng, postalCode); // pin lat lng of queried location onto map
            }

        } catch (Exception e) {
            e.printStackTrace();
            fetchLatLongFromService fetch_latlng = new fetchLatLongFromService(
                    postalCode.replaceAll("\\s+", ""));
            fetch_latlng.execute();

        }

    }

    /**
     * pins the given latlng object onto map
     */
    private void pinOnToMap(LatLng latLng, String markerTag) {
        System.out.println("pinning " + markerTag + " onto map");
        System.out.println("map: " + mMap);
        mMap.addMarker(new MarkerOptions().position(latLng).title(markerTag));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    /**
     * initialize the necessary libraries.
     */
    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getResources().getString(R.string.requestingLocationServices))) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(getResources().getString(R.string.requestingLocationServices));
            }

            if (savedInstanceState.containsKey(getResources().getString(R.string.lastKnownLocation))) {
                mCurrentLocation = savedInstanceState.getParcelable(getResources().getString(R.string.lastKnownLocation));
            }
        }

        updateLocationUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state of map
        outState.putBoolean(getResources().getString(R.string.requestingLocationServices), mRequestingLocationUpdates);
        outState.putParcelable(getResources().getString(R.string.lastKnownLocation), mCurrentLocation);
    }

    /**
     * Update the UI displaying the location data
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            System.out.println("current location found!");
            // add pin at current location
            LatLng currentLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            System.out.println(currentLoc.toString());
            pinOnToMap(currentLoc, getResources().getString(R.string.currentLocation));
        }
    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        System.out.println("starting location updates");
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("location services", "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("location services", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(CourierMapsActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("location services", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("location services", errorMessage);

                                Toast.makeText(CourierMapsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    /**
     * stops updating location
     */
    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
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
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLocationUI();
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

        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // request connection to update UI
        requestConnectionToLocationServices();

        // retrieve list of pending orders to update mapview
        mapOrders();
    }

    //Sometimes happens that device gives location = null

    public class fetchLatLongFromService extends
            AsyncTask<Void, Void, StringBuilder> {
        String place;


        public fetchLatLongFromService(String place) {
            super();
            this.place = place;

        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            this.cancel(true);
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="
                        + this.place + "&sensor=false";

                URL url = new URL(googleMapUrl);
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
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                JSONArray resultJsonArray = jsonObj.getJSONArray("results");

                // Extract the Place descriptions from the results
                // resultList = new ArrayList<String>(resultJsonArray.length());

                JSONObject before_geometry_jsonObj = resultJsonArray
                        .getJSONObject(0);

                JSONObject geometry_jsonObj = before_geometry_jsonObj
                        .getJSONObject("geometry");

                JSONObject location_jsonObj = geometry_jsonObj
                        .getJSONObject("location");

                String lat_helper = location_jsonObj.getString("lat");
                double lat = Double.valueOf(lat_helper);


                String lng_helper = location_jsonObj.getString("lng");
                double lng = Double.valueOf(lng_helper);


                LatLng point = new LatLng(lat, lng);


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }

}
