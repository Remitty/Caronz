package com.remitty.caronz.helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.remitty.caronz.App;
import com.remitty.caronz.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GMSHelper {

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Log.e("Utilis", "My Current: " + addresses.toString());
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                if (returnedAddress.getMaxAddressLineIndex() > 0) {

                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                    }
                } else {
                    strReturnedAddress.append(returnedAddress.getAddressLine(0)).append("");
                }
                strAdd = strReturnedAddress.toString();
                Log.d("Utilis My Current", "" + strReturnedAddress.toString());
            } else {
                Log.d("Utilis My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Utilis My Current", "Canont get Address!");
        }
        return strAdd;
    }

    public static String getPlaceAutoCompleteUrl(String input, Double latitude, Double longitude, String apiKey) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=500&language=en");
        urlString.append("&key=" + apiKey);

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

    public static String getPlaceAutoLatlong(String input, String apiKey) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/details/json");
        urlString.append("?placeid=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //urlString.append("&location=");
        //urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        //urlString.append("&radius=500&language=en");
        urlString.append("&key=" + apiKey);

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

//    public static ArrayList getPlaces(String input, String apiKey) throws JSONException {
//        //code for API level 23 as httpclient is depricated in API 23
//        StringBuffer sb=null;
//        URL url;
//        HttpURLConnection urlConnection = null;
//        String constraint = getPlaceAutoLatlong(input, apiKey);
//        try {
//            url = new URL(constraint);
//            urlConnection = (HttpURLConnection) url.openConnection();
//            InputStream in = urlConnection.getInputStream();
//            InputStreamReader isw = new InputStreamReader(in);
//            int data = isw.read();
//            sb=new StringBuffer("");
//            while (data != -1) {
//                sb.append((char)data);
//                //char current = (char) data;
//                data = isw.read();
//                // System.out.print(current);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }
//        Log.v("temp is", "" + sb);
//        if(sb != null)
//        return   parseGoogleParse(sb.toString());
//        else return null;
//    }

    // method to parse the json returned by googleplaces api
    public static ArrayList parseGoogleParse(final String response) throws JSONException {
        ArrayList temp = new ArrayList();
        try {
            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);
            Log.v("json is", "" + jsonObject);
            Gson gson = new Gson();
            String Addr = String.valueOf(jsonObject.getJSONObject("result").getString("formatted_address"));
            String latu = String.valueOf(jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getString("lat"));
            String longu = String.valueOf(jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getString("lng"));
            temp.add(Addr);
            temp.add(latu);
            temp.add(longu);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
        return temp;
    }
}
