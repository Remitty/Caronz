package com.remitty.caronz.gms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.remitty.caronz.App;
import com.remitty.caronz.R;
import com.remitty.caronz.adapters.AutoCompleteAdapter;
import com.remitty.caronz.helper.GMSHelper;
import com.remitty.caronz.models.PlacePredictions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AddHomeWorkActivity  extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ListView mAutoCompleteList;
    private RecyclerView rvRecentResults;
    private EditText txtLocation;
    private PlacePredictions predictions = new PlacePredictions();
    private AutoCompleteAdapter mAutoCompleteAdapter;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private PlacePredictions placePredictions = new PlacePredictions();
    private GoogleApiClient mGoogleApiClient;
    private Handler handler;
    private Location mLastLocation;
    double latitude;
    double longitude;
    ImageView backArrow;
    private String GETPLACESHIT = "places_hit";
    private String strTag = "";
    String Addr, latu, longu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_home_work);
        strTag = getIntent().getExtras().getString("tag");
        init();
    }

    private void init() {
        rvRecentResults = (RecyclerView) findViewById(R.id.rvRecentResults);
        mAutoCompleteList = (ListView) findViewById(R.id.searchResultLV);
        txtLocation = (EditText) findViewById(R.id.txtLocation);
        backArrow = (ImageView) findViewById(R.id.backArrow);

        txtLocation.requestFocus();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mAutoCompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setGoogleAddress(position);
            }
        });

        //get permission for Android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fetchLocation();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);
            } else {
                fetchLocation();
            }
        }
        //Add a text change listener to implement autocomplete functionality
        txtLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtLocation.getText().length() > 0) {
                    rvRecentResults.setVisibility(View.GONE);
                    if (mAutoCompleteAdapter == null)
                        mAutoCompleteList.setVisibility(View.VISIBLE);

                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            App.getInstance().cancelRequestInQueue(GETPLACESHIT);
                            JSONObject object = new JSONObject();
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GMSHelper.getPlaceAutoCompleteUrl(s+"", latitude, longitude, getResources().getString(R.string.places_api_key)),
                                    object, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.v("PayNowRequestResponse", response.toString());
                                    Log.v("PayNowRequestResponse", response.toString());
                                    Gson gson = new Gson();
                                    predictions = gson.fromJson(response.toString(), PlacePredictions.class);
                                    if (mAutoCompleteAdapter == null) {
                                        mAutoCompleteAdapter = new AutoCompleteAdapter(AddHomeWorkActivity.this, predictions.getPlaces(), AddHomeWorkActivity.this);
                                        mAutoCompleteList.setAdapter(mAutoCompleteAdapter);
                                    } else {
                                        mAutoCompleteList.setVisibility(View.VISIBLE);
                                        mAutoCompleteAdapter.clear();
                                        mAutoCompleteAdapter.addAll(predictions.getPlaces());
                                        mAutoCompleteAdapter.notifyDataSetChanged();
                                        mAutoCompleteList.invalidate();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.v("PayNowRequestResponse", error.toString());
                                }
                            });
                            App.getInstance().addToRequestQueue(jsonObjectRequest);
                        }
                    };

                    // only canceling the network calls will not help, you need to remove all callbacks as well
                    // otherwise the pending callbacks and messages will again invoke the handler and will send the request
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                    } else {
                        handler = new Handler();
                    }
                    handler.postDelayed(run, 1000);

                }else{
                    mAutoCompleteList.setVisibility(View.GONE);
                    rvRecentResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private void setGoogleAddress(int position) {
        final String PlID = predictions.getPlaces().get(position).getPlaceID();

            Log.e("Place ID == >", PlID);
        App.getInstance().cancelRequestInQueue(GETPLACESHIT);
        String url = GMSHelper.getPlaceAutoLatlong(PlID,  getResources().getString(R.string.places_api_key));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            ArrayList<String> place = GMSHelper.parseGoogleParse(response);
                            if(place != null) {
                                Addr = place.get(0);
                                latu = place.get(1);
                                longu = place.get(2);
                            }

                            Log.v("Latitude is", "" + Addr);

                            placePredictions.strSourceAddress = Addr;
                            placePredictions.strSourceLatLng = String.valueOf(latu) + ","+ String.valueOf(longu);
                            placePredictions.strSourceLatitude = String.valueOf(latu);
                            placePredictions.strSourceLongitude = String.valueOf(longu);
                            mAutoCompleteAdapter = null;

                            mAutoCompleteList.setVisibility(View.GONE);
                            setAddress();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        App.getInstance().addToRequestQueue(stringRequest);
    }

    void setAddress() {
        hideSoftKeyboard(getCurrentFocus());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (placePredictions != null) {
                    intent.putExtra("Location Address", placePredictions);
                    intent.putExtra("strTag", strTag);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
            }
        }, 500);
    }

    public void fetchLocation() {
        //Build google API client to use fused location
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    fetchLocation();
                } else {
                    // permission denied!
                    Toast.makeText(this, "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

