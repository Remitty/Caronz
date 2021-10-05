package com.remitty.caronz.avis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.avis.models.AvisCar;
import com.remitty.caronz.avis.models.AvisLocation;
import com.remitty.caronz.payment.StripePayment;
import com.remitty.caronz.payment.Thankyou;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.views.BannerSlider;

public class AvisCarDetailActivity extends AppCompatActivity implements RuntimePermissionHelper.permissionInterface {
    SettingsMain settingsMain;
    RestService restService;
    RuntimePermissionHelper runtimePermissionHelper;

    TextView textViewAdName, tvCatName, tvPrice, tvCurrency, tvSeat, tvCarLocation, tvCarLocationName, tvCarTransmission;
    LinearLayout bluetoothLayout, airConLayout, smokeLayout;
    Button btnBook;

    BannerSlider bannerSlider;
    List<Banner> banners = new ArrayList<>();
    ArrayList<String> imageUrls = new ArrayList<>();

    AvisCar car;
    private AvisLocation pickupLocation, dropoffLocation;
    private LinearLayout callLayout;
    private TextView tvSpeed;

    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avis_car_detail);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);
        runtimePermissionHelper = new RuntimePermissionHelper(AvisCarDetailActivity.this, this);

        initComponents();

        if (getIntent() != null) {
            if(getIntent().hasExtra("bookId")) {
                bookId = getIntent().getStringExtra("bookId");
                requestReservation();
                btnBook.setVisibility(View.GONE);
            } else {
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

                setViews();
            }
        }

        initListeners();


    }

    private void initComponents() {
        bannerSlider = findViewById(R.id.banner_slider1);

        callLayout = findViewById(R.id.ll_call);

        textViewAdName = findViewById(R.id.tv_car_name);
        tvCatName = findViewById(R.id.tv_car_cat);
        tvPrice = findViewById(R.id.tv_price);
        tvCurrency = findViewById(R.id.tv_currency);

        tvSpeed = findViewById(R.id.tv_car_speed);
        tvSeat = findViewById(R.id.tv_car_seat);
        tvCarLocation = findViewById(R.id.tv_car_location);
        tvCarLocationName = findViewById(R.id.tv_car_location_name);
        tvCarTransmission = findViewById(R.id.tv_car_transmission);

        bluetoothLayout = findViewById(R.id.tv_car_bluetooth);
        airConLayout = findViewById(R.id.tv_car_aircon);
        smokeLayout = findViewById(R.id.tv_car_smoke);

        btnBook = findViewById(R.id.btn_book);
    }

    private void setViews() {
        banners.clear();
        imageUrls.clear();
//        if (bannerSlider != null)
//            bannerSlider.removeAllBanners();

        String path = car.getCatImage();
        banners.add(new RemoteBanner(path));
        imageUrls.add(path);
        banners.get(0).setScaleType(ImageView.ScaleType.FIT_XY);

        if (banners.size() > 0 && bannerSlider != null)
            bannerSlider.setBanners(banners);

        tvCatName.setText(car.getMake());
        textViewAdName.setText(car.getModel());

        tvSpeed.setText(car.getDoors() + " Doors");
        tvPrice.setText(car.getPrice());
        tvCurrency.setText(car.getCurrency());
        tvCarLocation.setText(pickupLocation.getName());
        tvCarLocationName.setText(pickupLocation.getAddress());

        tvSeat.setText(car.getSeats() + " Seats");
        tvCarTransmission.setText(car.getTransmission());

        if(!car.hasBluetooth())
            bluetoothLayout.setVisibility(View.INVISIBLE);
        if(!car.isSmokeFree())
            smokeLayout.setVisibility(View.INVISIBLE);
        if(!car.hasAirCondition())
            airConLayout.setVisibility(View.INVISIBLE);

    }

    private void initListeners() {
        callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settingsMain.getAppOpen()) {
                    Toast.makeText(getBaseContext(), settingsMain.getNoLoginMessage(), Toast.LENGTH_SHORT).show();

                } else {
                    runtimePermissionHelper.requestCallPermission(1);
                }
            }
        });

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AvisCarDetailActivity.this, AvisReserveActivity.class);
                intent.putExtra("car", car);
                intent.putExtra("pickup", pickupLocation);
                intent.putExtra("dropoff", pickupLocation);
                intent.putExtra("dropoff_time", getIntent().getStringExtra("dropoff_time"));
                intent.putExtra("pickup_time", getIntent().getStringExtra("pickup_time"));
                intent.putExtra("brand", getIntent().getStringExtra("brand"));
                startActivity(intent);
            }
        });
    }


    private void requestReservation() {
        settingsMain.showDilog(this);

        if (SettingsMain.isConnectingToInternet(this)) {

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookId);

            Log.e("avis search params", params.toString());

            Call<ResponseBody> myCall = restService.getAvis(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info load realty Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            JSONObject data = response.getJSONObject("data");
                            car = new AvisCar();
                            car.setCar(data.getJSONObject("vehicle"));
                            car.setRate(data.getJSONObject("rate_totals"));
                            pickupLocation = new AvisLocation();
                            pickupLocation.setData(data.getJSONObject("pickup_location"));
                            pickupLocation.setLocation(data.getJSONObject("pickup_location").getJSONObject("location"));

                            setViews();
                        } else {
                            Log.e("avis search issue: " , responseObj.errorBody().string());
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
                    }
                    else {
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

    public void Call() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + pickupLocation.getPhone()));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    @Override
    public void onSuccessPermission(int code) {
        Call();
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
