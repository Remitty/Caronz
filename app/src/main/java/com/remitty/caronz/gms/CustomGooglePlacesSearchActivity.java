package com.remitty.caronz.gms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.remitty.caronz.App;
import com.remitty.caronz.R;
import com.remitty.caronz.adapters.AutoCompleteAdapter;
import com.remitty.caronz.helper.GMSHelper;
import com.remitty.caronz.helper.SharedHelper;
import com.remitty.caronz.models.PlacePredictions;
import com.remitty.caronz.models.RecentAddressData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static java.lang.Double.parseDouble;

public class CustomGooglePlacesSearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final int PICKUP_LOCATION = 1000;
    private EditText editLocation;
    private ListView mAutoCompleteList;
    private String GETPLACESHIT = "places_hit";
    private PlacePredictions predictions = new PlacePredictions();
    private PlacePredictions placePredictions = new PlacePredictions();

    private AutoCompleteAdapter mAutoCompleteAdapter;
    private Location mLastLocation;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private Handler handler;
    private GoogleApiClient mGoogleApiClient;
    TextView txtPickLocation;
    ImageView backArrow, imgClose;
    LinearLayout lnrFavorite;
    Activity thisActivity;

    Bundle extras;
    TextView txtHomeLocation, txtWorkLocation;
    private int UPDATE_HOME_WORK = 1;
    LinearLayout lnrHome, lnrWork;
    ArrayList<RecentAddressData> lstRecentList = new ArrayList<RecentAddressData>();
    RelativeLayout rytAddressSource;

    RecyclerView rvRecentResults;

    String strSource = "";
    String strSelected = "";

    double latitude;
    double longitude;
    static String Addr,latu,longu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_google_places_search);

        thisActivity = CustomGooglePlacesSearchActivity.this;

        editLocation = (EditText) findViewById(R.id.edit_location);

        backArrow = (ImageView) findViewById(R.id.backArrow);
        imgClose = (ImageView) findViewById(R.id.imgClose);

        txtPickLocation = (TextView) findViewById(R.id.txtPickLocation);
        txtWorkLocation = (TextView) findViewById(R.id.txtWorkLocation);
        txtHomeLocation = (TextView) findViewById(R.id.txtHomeLocation);

        lnrFavorite = (LinearLayout) findViewById(R.id.lnrFavorite);
        lnrHome = (LinearLayout) findViewById(R.id.lnrHome);
        lnrWork = (LinearLayout) findViewById(R.id.lnrWork);

        rytAddressSource = (RelativeLayout) findViewById(R.id.rytAddressSource);

        rvRecentResults = (RecyclerView) findViewById(R.id.rvRecentResults);
        mAutoCompleteList = (ListView) findViewById(R.id.searchResultLV);

        String cursor = getIntent().getStringExtra("cursor");
        String s_address = getIntent().getStringExtra("s_address");
        String d_address = getIntent().getStringExtra("d_address");
        Log.e("CustomGoogleSearch", "onCreate: source " + s_address);
        Log.e("CustomGoogleSearch", "onCreate: destination" + d_address);

        if (cursor.equals("destination") && d_address != null && !d_address.equalsIgnoreCase("")) {
            editLocation.setText(d_address);
        }

        if (cursor.equals("source") && s_address != null && !s_address.equalsIgnoreCase("")) {
            editLocation.setText(s_address);
        }

        editLocation.requestFocus();
        imgClose.setVisibility(View.VISIBLE);
        if (cursor.equalsIgnoreCase("source")) {
            strSelected = "source";
        } else {
            strSelected = "destination";
        }

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

        initListeners();
    }

    private void initListeners() {

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setAddress();
                finish();
            }
        });

        editLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imgClose.setVisibility(View.VISIBLE);
                } else {
                    imgClose.setVisibility(View.GONE);
                }
            }
        });


        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLocation.setText("");
                mAutoCompleteList.setVisibility(View.GONE);
                imgClose.setVisibility(View.GONE);
            }
        });


        txtPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(CustomGooglePlacesSearchActivity.this, PickupLocationActivity.class);
                        startActivityForResult(intent, PICKUP_LOCATION);
                    }
                }, 500);
            }
        });

        lnrHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "home").equalsIgnoreCase("")){
                    gotoHomeWork("home");
                }else{

                    placePredictions.strSourceAddress = SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "home");
                    placePredictions.strSourceLatitude = SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "home_lat");
                    placePredictions.strSourceLongitude = SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "home_lng");
                    LatLng latlng = new LatLng(parseDouble( placePredictions.strSourceLatitude), parseDouble( placePredictions.strSourceLongitude));
                    placePredictions.strSourceLatLng = "" + latlng;
                    editLocation.setText(placePredictions.strSourceAddress);
                    editLocation.setSelection(0);
//                    editLocation.requestFocus();
                    mAutoCompleteAdapter = null;

                    setAddress();
                }
            }
        });

        lnrWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "work").equalsIgnoreCase("")){
                    gotoHomeWork("work");
                }else{

                    placePredictions.strSourceAddress = SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "work");
                    placePredictions.strSourceLatitude = SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "work_lat");
                    placePredictions.strSourceLongitude = SharedHelper.getKey(CustomGooglePlacesSearchActivity.this, "work_lng");
                    LatLng latlng = new LatLng(parseDouble( placePredictions.strSourceLatitude), parseDouble( placePredictions.strSourceLongitude));
                    placePredictions.strSourceLatLng = "" + latlng;
                    editLocation.setText(placePredictions.strSourceAddress);
                    editLocation.setSelection(0);
//                    editLocation.requestFocus();
                    mAutoCompleteAdapter = null;

                    setAddress();
                }
            }
        });

        editLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                imgClose.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // optimised way is to start searching for laction after user has typed minimum 3 chars
                imgClose.setVisibility(View.VISIBLE);
                strSelected = "destination";
                if (editLocation.getText().length() > 0) {
                    lnrFavorite.setVisibility(View.GONE);
//                    txtPickLocation.setVisibility(View.VISIBLE);
                    imgClose.setVisibility(View.VISIBLE);
                    txtPickLocation.setText(getString(R.string.pin_location));
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            App.getInstance().cancelRequestInQueue(GETPLACESHIT);

                            JSONObject object = new JSONObject();
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GMSHelper.getPlaceAutoCompleteUrl(s+"", latitude, longitude, getResources().getString(R.string.places_api_key)), object, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.v("PayNowRequestResponse", response.toString());
                                    Log.v("PayNowRequestResponse", response.toString());
                                    Gson gson = new Gson();
                                    predictions = gson.fromJson(response.toString(), PlacePredictions.class);
                                    if (mAutoCompleteAdapter == null) {
                                        mAutoCompleteList.setVisibility(View.VISIBLE);
                                        mAutoCompleteAdapter = new AutoCompleteAdapter(CustomGooglePlacesSearchActivity.this, predictions.getPlaces(), CustomGooglePlacesSearchActivity.this);
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
                    lnrFavorite.setVisibility(View.VISIBLE);
                    mAutoCompleteList.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                imgClose.setVisibility(View.VISIBLE);
            }

        });

        //txtDestination.setText("");
        editLocation.setSelection(editLocation.getText().length());

        mAutoCompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (editLocation.getText().toString().equalsIgnoreCase("")) {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
                        LayoutInflater inflater = (LayoutInflater) thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        builder.setMessage("Please choose pickup location")
                                .setTitle(thisActivity.getString(R.string.app_name))
                                .setCancelable(true)
                                .setIcon(R.mipmap.ic_launcher_round)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        editLocation.requestFocus();
                                        editLocation.setText("");
                                        imgClose.setVisibility(View.GONE);
                                        mAutoCompleteList.setVisibility(View.GONE);
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        setGoogleAddressNew(position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    private void setGoogleAddressNew(final int position) throws JSONException {
        final String PlID = predictions.getPlaces().get(position).getPlaceID();
        // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
        App.getInstance().cancelRequestInQueue(GETPLACESHIT);
        Log.e("pid is", "" + PlID);
        //        ArrayList<String> place = GMSHelper.getPlaces(PlID, getResources().getString(R.string.places_api_key));

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
                            editLocation.setText(placePredictions.strSourceAddress);
                            editLocation.setSelection(0);
                            editLocation.requestFocus();
                            mAutoCompleteAdapter = null;

                            mAutoCompleteList.setVisibility(View.GONE);

                            if (editLocation.getText().toString().length() > 0) {
                                //places.release();
                                if (strSelected.equalsIgnoreCase("destination")) {
                                    if (!placePredictions.strDestAddress.equalsIgnoreCase(placePredictions.strSourceAddress)) {
                                        setAddress();
                                    } else {
                                        Toast.makeText(thisActivity, "address not so accurate or should not be same!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                editLocation.requestFocus();
                                editLocation.setText("");
                                imgClose.setVisibility(View.GONE);
                                mAutoCompleteList.setVisibility(View.GONE);
                            }

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

    public void fetchLocation() {
        //Build google API client to use fused location
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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

    private void gotoHomeWork(String strTag) {
        Intent intentHomeWork = new Intent(CustomGooglePlacesSearchActivity.this, AddHomeWorkActivity.class);
        intentHomeWork.putExtra("tag", strTag);
        startActivityForResult(intentHomeWork, UPDATE_HOME_WORK);
    }

    void setAddress() {
        hideSoftKeyboard(getCurrentFocus());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (placePredictions != null) {
                    intent.putExtra("Location Address", placePredictions);
//                    intent.putExtra("pick_location", "no");
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
            }
        }, 500);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKUP_LOCATION) {

            if (resultCode == Activity.RESULT_OK) {

//                PlacePredictions placePredictions;
                placePredictions = (PlacePredictions) data.getSerializableExtra("Location Address");
//                strPickLocation = data.getExtras().getString("pick_location");
//                strPickType = data.getExtras().getString("type");

                if (placePredictions != null) {

                    if (!placePredictions.strSourceAddress.equalsIgnoreCase("")) {
                        try {
                            latitude = Double.parseDouble(placePredictions.strSourceLatitude);
                            longitude =  Double.parseDouble(placePredictions.strSourceLongitude);
                            String pickupLocation = GMSHelper.getCompleteAddressString(this, latitude, longitude);
                            editLocation.setText(pickupLocation);
                            setAddress();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
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
    public void onConnected(@Nullable Bundle bundle) {
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
}
