package com.remitty.caronz.avis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.JsonObject;
import com.jarklee.materialdatetimepicker.date.DatePickerDialog;
import com.jarklee.materialdatetimepicker.time.RadialPickerLayout;
import com.jarklee.materialdatetimepicker.time.TimePickerDialog;
import com.remitty.caronz.R;
import com.remitty.caronz.avis.adapters.AutoCompleteLocationAdapter;
import com.remitty.caronz.avis.models.AvisCar;
import com.remitty.caronz.avis.models.AvisLocation;
import com.remitty.caronz.gms.CustomGooglePlacesSearchActivity;
import com.remitty.caronz.helper.GMSHelper;
import com.remitty.caronz.home.AddNewAdPost;
import com.remitty.caronz.models.PlacePredictions;
import com.remitty.caronz.utills.GPSTracker;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

public class AvisPickupActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST = 1000;
    RestService restService;

    EditText editArriveDate, editArriveTime, editDropoffDate, editDropoffTime;
    private EditText txtDestination, txtaddressSource;
    Button btnSearch;

    ListView s_RecyclerView, d_RecyclerView;
    AutoCompleteLocationAdapter mAdapter;
    ArrayList<AvisLocation> locationList = new ArrayList<>();

    private String strSelected;
    private AvisLocation pickupLocation, dropoffLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avis_pickup);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        restService = UrlController.createService(RestService.class);

        txtDestination = findViewById(R.id.txtDestination);
        txtaddressSource = findViewById(R.id.txtaddressSource);
        editArriveTime = findViewById(R.id.edit_arrive_time);
        editArriveDate = findViewById(R.id.edit_arrive_date);
        editDropoffTime = findViewById(R.id.edit_dropoff_time);
        editDropoffDate = findViewById(R.id.edit_dropoff_date);
        btnSearch = findViewById(R.id.btn_search);

        s_RecyclerView = findViewById(R.id.s_recyclerview);
        d_RecyclerView = findViewById(R.id.d_recyclerview);
        d_RecyclerView.setVisibility(View.GONE);
        s_RecyclerView.setVisibility(View.GONE);
        mAdapter = new AutoCompleteLocationAdapter(AvisPickupActivity.this, locationList);
        s_RecyclerView.setAdapter(mAdapter);
        d_RecyclerView.setAdapter(mAdapter);



        initListeners();
    }

    private void initListeners() {
        editArriveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                strSelected = "Pickup";
                showTimePickerDialog();
            }
        });

        editArriveDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                strSelected = "Pickup";
                showDatePickerDialog();
            }
        });

        editDropoffTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                strSelected = "Dropoff";
                showTimePickerDialog();
            }
        });

        editDropoffDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                strSelected = "Dropoff";
                showDatePickerDialog();
            }
        });

        txtDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                s_RecyclerView.setVisibility(View.GONE);
                d_RecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()) {
                    locationList.clear();
                    mAdapter.notifyDataSetChanged();
                    return;
                }
                searchLocations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtaddressSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                d_RecyclerView.setVisibility(View.GONE);
                s_RecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()) {
                    locationList.clear();
                    mAdapter.notifyDataSetChanged();
                    return;
                }
                searchLocations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        d_RecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dropoffLocation = locationList.get(position);
                txtDestination.setText(dropoffLocation.getAddress());
                d_RecyclerView.setVisibility(View.GONE);
                hideSoftKeyboard(view);
            }
        });

        s_RecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickupLocation = locationList.get(position);
                txtaddressSource.setText(pickupLocation.getAddress());
                s_RecyclerView.setVisibility(View.GONE);
                hideSoftKeyboard(view);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean validate = true;
                if(TextUtils.isEmpty(editArriveTime.getText())) {
                    editArriveTime.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(editArriveDate.getText())) {
                    editArriveDate.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(editDropoffTime.getText())) {
                    editDropoffTime.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(editDropoffDate.getText())) {
                    editDropoffDate.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(txtaddressSource.getText().toString())) {
                    txtaddressSource.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(txtDestination.getText().toString())) {
                    txtDestination.setError("!");
                    validate = false;
                }

                if(validate) {
                    searchCar();

                }

            }
        });
    }

    private void searchCar() {
        Intent intent = new Intent(AvisPickupActivity.this, AvisSearchActivity.class);
        intent.putExtra("pickup_time", editArriveDate.getText().toString() + " " + editArriveTime.getText().toString());
        intent.putExtra("dropoff_time", editDropoffDate.getText().toString() + " " + editDropoffTime.getText().toString());
        intent.putExtra("pickup", pickupLocation);
        intent.putExtra("dropoff", dropoffLocation);
        intent.putExtra("brand", getIntent().getStringExtra("brand"));
        startActivity(intent);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showTimePickerDialog() {
        Date currentTime = Calendar.getInstance().getTime();
        TimePickerDialog dpd = TimePickerDialog.newInstance(
                this, currentTime.getHours(), currentTime.getMinutes(), false
        );
        dpd.show(this.getSupportFragmentManager(), "Timepickerdialog");
    }

    private void showDatePickerDialog() {
        Date currentTime = Calendar.getInstance().getTime();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this, currentTime.getYear()+1900, currentTime.getMonth(), currentTime.getDate()
        );
        dpd.show(this.getSupportFragmentManager(), "Datepickerdialog");
    }

    private void searchLocations(String keyword) {
        JsonObject params = new JsonObject();
        params.addProperty("location", keyword);

        Log.d("avis locations", params.toString());

        Call<ResponseBody> myCall = restService.locationsAvis(params, UrlController.AddHeaders(this));
        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                try {
                    Log.d("info load realty Resp", "" + responseObj.toString());
                    if (responseObj.isSuccessful()) {

                        JSONObject response = new JSONObject(responseObj.body().string());
                        locationList.clear();

                        JSONArray cars = response.getJSONArray("data");
                        Log.e("locations", cars.toString());
                        for (int i = 0; i < cars.length(); i++) {
                            AvisLocation location = new AvisLocation();
                            location.setData(cars.getJSONObject(i));
                            locationList.add(location);
                        }

                        mAdapter.notifyDataSetChanged();

                    } else {
                        Log.e("avis locations issue: " , responseObj.errorBody().string());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }


    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String mins = minute < 10 ? "0"+minute : minute+"";
        if (strSelected.equals("Pickup"))
            editArriveTime.setText(hourOfDay + ":" + mins);
        else
            editDropoffTime.setText(hourOfDay + ":" + mins);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (strSelected.equals("Pickup"))
            editArriveDate.setText((monthOfYear+1) + "/" + dayOfMonth + "/" + year);
        else
            editDropoffDate.setText((monthOfYear+1) + "/" + dayOfMonth + "/" + year);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
