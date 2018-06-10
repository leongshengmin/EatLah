package com.eatlah.eatlah.helpers;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchDirectionsFromService extends AsyncTask<Void, Void, StringBuilder> {
    private String origin;
    private String destination;
    private ArrayList<String> directions;
    private OnTaskCompletedListener onTaskCompletedListener;

    private GoogleMap mMap;
    private Polyline polyline;

    public FetchDirectionsFromService(Location currentLocation, String destination, GoogleMap mMap, OnTaskCompletedListener onTaskCompletedListener) {
        origin = generateLocName(currentLocation);
        this.directions = new ArrayList<>();
        this.onTaskCompletedListener = onTaskCompletedListener;
        this.destination = generateLocName(destination);
        this.mMap = mMap;

        System.out.println("current location: " + origin + " dest: " + this.destination);
    }

    /**
     * generates valid location name for url using location
     *
     * @param location
     * @return
     */
    private String generateLocName(Location location) {
        return new StringBuilder()
                .append(Double.toString(location.getLatitude()))
                .append(",")
                .append(Double.toString(location.getLongitude()))
                .toString()
                .trim();
    }

    /**
     * generates valid location name for url using string
     *
     * @param destination
     * @return
     */
    private String generateLocName(String destination) {
        return destination.replaceAll(" ", "+").trim();
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

            String mapUrl = "https://maps.googleapis.com/maps/api/directions/json?origin="
                    + origin + "&destination=" + destination
                    + "&mode=transit&region=sg";

            URL url = new URL(mapUrl);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(
                    conn.getInputStream());
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            return jsonResults;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(StringBuilder result) {
        super.onPostExecute(result);

        if (polyline != null) mMap.clear();

        try {
            JSONObject jsonObj = new JSONObject(result.toString());

            System.out.println(jsonObj);

            JSONObject routes_jsonObj = jsonObj.getJSONArray("routes")
                    .getJSONObject(0);


            // routes/bounds
            JSONObject bounds_jsonObj = routes_jsonObj.getJSONObject("bounds");

            // ./northeast
            JSONObject neBounds_jsonObj = bounds_jsonObj.getJSONObject("northeast");

            // ./southwest
            JSONObject swBounds_jsonObj = bounds_jsonObj.getJSONObject("southwest");
            LatLng ne_latlng = new LatLng(neBounds_jsonObj.getDouble("lat"), neBounds_jsonObj.getDouble("lng"));
            LatLng sw_latlng = new LatLng(swBounds_jsonObj.getDouble("lat"), swBounds_jsonObj.getDouble("lng"));


            // routes/legs
            JSONArray legs_jsonArray = routes_jsonObj.getJSONArray("legs");
            JSONObject legs_jsonObj = legs_jsonArray.getJSONObject(0);

            // ./arrival_time
            JSONObject arrivalTime_jsonObj = legs_jsonObj.getJSONObject("arrival_time");
            String arrivalTime = arrivalTime_jsonObj.getString("text");

            // ./departure_time
            JSONObject departureTime_jsonObj = legs_jsonObj.getJSONObject("departure_time");
            String departureTime = departureTime_jsonObj.getString("text");

            // ./distance
            JSONObject distance_jsonObj = legs_jsonObj.getJSONObject("distance");
            String distance = distance_jsonObj.getString("text");

            // ./duration
            JSONObject duration_jsonObj = legs_jsonObj.getJSONObject("duration");
            String duration = duration_jsonObj.getString("text");

            // ./end_location
            JSONObject endLoc_jsonObj = legs_jsonObj.getJSONObject("end_location");
            String endLoc = legs_jsonObj.getString("end_address");
            LatLng endLoc_latlng = new LatLng(endLoc_jsonObj.getDouble("lat"),
                                              endLoc_jsonObj.getDouble("lng"));

            // ./start_location
            JSONObject startLoc_jsonObj = legs_jsonObj.getJSONObject("start_location");
            String startLoc = legs_jsonObj.getString("start_address");
            LatLng startLoc_latlng = new LatLng(startLoc_jsonObj.getDouble("lat"),
                                                startLoc_jsonObj.getDouble("lng"));

//            // pin start and end location onto map
//            Marker src_marker = mMap.addMarker(new MarkerOptions()
//                    .position(startLoc_latlng)
//                    .title("Current Location"));
//
//            src_marker.setSnippet(startLoc + "\n" +
//                    "Departure Time: " + departureTime +
//                    "\nArrival Time: " + arrivalTime +
//                    "\nDistance: " + distance +
//                    "\nDuration: " + duration);
//
//            mMap.addMarker(new MarkerOptions()
//                    .position(endLoc_latlng)
//                    .title("Collection Point")
//                    .snippet(endLoc)
//            );

            // set lat lng bounds
            LatLngBounds latLngBounds = new LatLngBounds(sw_latlng, ne_latlng);
            mMap.setLatLngBoundsForCameraTarget(latLngBounds);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 800, 10000, 5));

            // ./steps
            JSONArray steps_jsonArray = legs_jsonObj.getJSONArray("steps");
            System.out.println("outer steps json: " + steps_jsonArray);
            for (int j = 0; j < steps_jsonArray.length(); j++) {

                JSONObject steps_jsonObj = steps_jsonArray.getJSONObject(j);
                String dir1 = steps_jsonObj.getString("html_instructions");
                directions.add(Html.fromHtml(dir1).toString());
                System.out.println("outer steps jsonobj: " + steps_jsonObj);

                // ./steps/steps
                if (steps_jsonObj.has("steps")) {
                    JSONArray innerSteps_jsonArray = steps_jsonObj.getJSONArray("steps");
                    System.out.println("inner steps json: " + innerSteps_jsonArray);
                    for (int i = 0; i < innerSteps_jsonArray.length(); i++) {
                        JSONObject innerSteps_jsonObj = innerSteps_jsonArray.getJSONObject(i);
                        String dir = innerSteps_jsonObj.getString("html_instructions");
                        directions.add(Html.fromHtml(dir).toString());
                        System.out.println("inner steps jsonobj: " + innerSteps_jsonObj);
                    }
                }

            }


            // routes/overview_polyline
            JSONObject overviewPolylines = routes_jsonObj.getJSONObject("overview_polyline");
            // ./points
            String encodedStr = overviewPolylines.getString("points");
            List<LatLng> polyline_latlng = decodePoly(encodedStr);

            for (int z = 0; z < polyline_latlng.size() - 1; z++) {
                LatLng src = polyline_latlng.get(z);
                LatLng dest = polyline_latlng.get(z + 1);
                polyline = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                             new LatLng(dest.latitude, dest.longitude))
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true)
                );
            }

            onTaskCompletedListener.onTaskCompleted(directions);

        } catch (JSONException e) {
            Log.e("json", e.getMessage());
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}
