package com.remitty.caronz.avis;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.avis.models.AvisCar;
import com.remitty.caronz.avis.models.AvisLocation;
import com.remitty.caronz.payment.Thankyou;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvisReserveActivity extends AppCompatActivity {

    SettingsMain settingsMain;
    RestService restService;

    TextView tvPickupTime, tvPickupLocation, tvDropoffTime, tvDropoffLocation, tvTotalPrice;
    EditText editFirstName, editLastName;
    Button btnPay;

    private AvisCar car;
    private AvisLocation pickupLocation, dropoffLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avis_reserve);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        if (getIntent() != null) {
            if (getIntent().hasExtra("car")) {
                car = (AvisCar) getIntent().getSerializableExtra("car");
                try {
                    car.setData(new JSONObject(car.jsonData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(getIntent().hasExtra("pickup")) {
                pickupLocation = (AvisLocation) getIntent().getSerializableExtra("pickup");
                try {
                    pickupLocation.setData(new JSONObject(pickupLocation.jsonData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(getIntent().hasExtra("dropoff")) {
                dropoffLocation = (AvisLocation) getIntent().getSerializableExtra("dropoff");
                try {
                    dropoffLocation.setData(new JSONObject(dropoffLocation.jsonData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        tvPickupLocation = findViewById(R.id.tv_pickup_location);
        tvPickupTime = findViewById(R.id.tv_pickup_time);
        tvDropoffTime = findViewById(R.id.tv_dropoff_time);
        tvDropoffLocation = findViewById(R.id.tv_dropoff_location);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnPay = findViewById(R.id.btn_book);

        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);

        tvPickupLocation.setText(pickupLocation.getName());
        tvDropoffLocation.setText(dropoffLocation.getName());
        tvPickupTime.setText(getIntent().getStringExtra("pickup_time"));
        tvDropoffTime.setText(getIntent().getStringExtra("dropoff_time"));

        tvTotalPrice.setText(car.getCurrency() + " " + car.getPrice());

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editFirstName.getText().toString().isEmpty()) {
                    editFirstName.setError("!");
                    return;
                }
                if(editLastName.getText().toString().isEmpty()) {
                    editLastName.setError("!");
                    return;
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(AvisReserveActivity.this);
                alert.setIcon(R.mipmap.ic_launcher)
                        .setTitle("Confirm Booking")
                        .setMessage("Are you sure you want to book this car?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bookCar();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


    }

    private void bookCar() {
        settingsMain.showDilog(this);

        if (SettingsMain.isConnectingToInternet(this)) {

            JsonObject params = new JsonObject();
            params.addProperty("brand", getIntent().getStringExtra("brand"));
            params.addProperty("pickup_date", getIntent().getStringExtra("pickup_time"));
            params.addProperty("pickup_location_code", pickupLocation.getCode());
            params.addProperty("dropoff_date", getIntent().getStringExtra("dropoff_time"));
            params.addProperty("dropoff_location_code", dropoffLocation.getCode());
            params.addProperty("vehicle_class_code", car.getVehicleClassCode());
            params.addProperty("rate_code", car.getRateCode());
            params.addProperty("country_code", pickupLocation.getCountry());
            params.addProperty("first_name", editFirstName.getText().toString());
            params.addProperty("last_name", editLastName.getText().toString());

            Log.e("avis book params", params.toString());

            Call<ResponseBody> myCall = restService.bookAvis(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info load realty Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Toast.makeText(getBaseContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                            thankYou();

                        } else {
                            Log.e("avis search issue: ", responseObj.errorBody().string());
                            settingsMain.showAlertDialog(AvisReserveActivity.this, responseObj.errorBody().string());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    loadingLayout.setVisibility(View.GONE);
                    settingsMain.hideDilog();
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    } else {
                        Toast.makeText(getBaseContext(), "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            settingsMain.hideDilog();
            Toast.makeText(this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void thankYou() {
        Intent intent = new Intent(AvisReserveActivity.this, Thankyou.class);
        intent.putExtra("order_thankyou_title", "Congratulation");
        intent.putExtra("order_thankyou_btn", "Home");
        startActivity(intent);
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
