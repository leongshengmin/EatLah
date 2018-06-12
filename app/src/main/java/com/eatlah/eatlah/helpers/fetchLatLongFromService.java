package com.eatlah.eatlah.helpers;

import android.os.AsyncTask;

import com.eatlah.eatlah.models.Order;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class fetchLatLongFromService extends
        AsyncTask<Void, Void, StringBuilder> {
    String place;
    HashMap<String, Order> orderDict;
    GoogleMap mMap;
    OnTaskCompletedListener onTaskCompletedListener;


    public fetchLatLongFromService(String place, HashMap<String, Order> orderDict, GoogleMap mMap) {
        super();
        this.mMap = mMap;
        this.orderDict = orderDict;
        this.place = place;
    }

    public fetchLatLongFromService(String place, GoogleMap mMap) {
        this(place, null, mMap);
    }

    public fetchLatLongFromService(String place, OnTaskCompletedListener onTaskCompletedListener) {
        this(place, null, null);
        this.onTaskCompletedListener = onTaskCompletedListener;
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

            if (orderDict != null && mMap != null) {
                String time = String.format("Collection Time: %s", orderDict.get(place).getCollectionTime());
                pinOnToMap(point, postal, "Collection Location", "Address: " + address + "\nCollection time: " + time);
            } else if (mMap != null) {
                pinOnToMap(point, postal, "Delivery Location", address);
            }

            if (onTaskCompletedListener != null) {
                onTaskCompletedListener.onTaskCompleted(address);
            }

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    /**
     * pins the given latlng object onto map
     */
    private void pinOnToMap(LatLng latLng, String markerTag, String title, String snippet) {
        System.out.println("pinning " + markerTag + " onto map");
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        marker.setTag(markerTag);
        marker.setSnippet(snippet);
    }

    private String buildAddress(String addresst, String city, String postal) {
        return String.format("%s, %s %s", addresst, city, postal);
    }
}