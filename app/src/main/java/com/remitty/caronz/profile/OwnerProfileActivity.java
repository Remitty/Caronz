package com.remitty.caronz.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.adapters.CarAdapter;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.UserModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerProfileActivity extends AppCompatActivity implements RuntimePermissionHelper.permissionInterface {

    SettingsMain settingsMain;
    RestService restService;
    RuntimePermissionHelper runtimePermissionHelper;

    private ArrayList<CarModel> carsList = new ArrayList<>();
    RecyclerView mRecyclerView;
    CarAdapter carAdapter;

    TextView textViewUserName, textViewEmailvalue, textViewPhonevalue, textViewLocationvalue, tvAddress;
    ImageView imageViewProfile;
    ImageView btnCall;

    private Integer userId;

    UserModel profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class);
        runtimePermissionHelper = new RuntimePermissionHelper(this, this);

        if (getIntent() != null && getIntent().hasExtra("userId"))
            userId = getIntent().getIntExtra("userId", 0);

        textViewUserName = findViewById(R.id.text_viewName);

        imageViewProfile = findViewById(R.id.image_view);
//        ratingBar = findViewById(R.id.ratingBar);

//        LayerDrawable stars = (LayerDrawable) this.ratingBar.getProgressDrawable();
//        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

        btnCall = findViewById(R.id.btn_call);

        textViewEmailvalue = findViewById(R.id.textViewEmailValue);
        textViewPhonevalue = findViewById(R.id.textViewPhoneValue);
        textViewLocationvalue = findViewById(R.id.tv_location);
        tvAddress = findViewById(R.id.tv_address);


        carAdapter = new CarAdapter(OwnerProfileActivity.this, carsList, "");
        mRecyclerView = findViewById(R.id.related_recycler_view);
//        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(carAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        setAllViewsText();

        btnCall.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!profile.getMobile().isEmpty()) {
                    runtimePermissionHelper.requestCallPermission(100);

                } else {
                    Toast.makeText(getBaseContext(), "No seller phone number.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void setAllViewsText() {

        if (SettingsMain.isConnectingToInternet(this)) {

            SettingsMain.showDilog(this);
            JsonObject prams = new JsonObject();
            prams.addProperty("userId", userId);
            Log.e("seller id params: ", prams.toString());
            Call<ResponseBody> myCall = restService.getSellerProfile(prams, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Edit Profile ", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info Edit ProfileGet", "" + response.getJSONObject("data"));

                                JSONObject jsonObject = response.getJSONObject("data");

                                profile = new UserModel(jsonObject);

                                Picasso.with(getBaseContext()).load(settingsMain.getUserImage())
                                        .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(imageViewProfile);

                                textViewUserName.setText(profile.getFirstName() + " " + profile.getLastName());
                                textViewEmailvalue.setText(profile.getEmail());

                                textViewPhonevalue.setText(profile.getPhone());
                                textViewLocationvalue.setText(profile.getLocation());
                                tvAddress.setText(profile.getFirstAddress() + ", " + profile.getSecondAddress());

                                JSONArray carsArray = response.getJSONArray("related_cars");
                                carsList.clear();
                                for (int i = 0; i < carsArray.length(); i++) {
                                    CarModel car = new CarModel(carsArray.getJSONObject(i));
                                    carsList.add(car);
                                }
                                carAdapter.loadMore(carsList);

                            } else {
                                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Log.d("info Edit Profile error", String.valueOf(t));
                    Log.d("info Edit Profile error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public void onSuccessPermission(int code) {
        if (code == 100) {

            Intent intent1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + profile.getPhone()));
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
            startActivity(intent1);
        }
     }
 }
