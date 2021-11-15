package com.remitty.caronz.avis;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.avis.adapters.AvisCarAdapter;
import com.remitty.caronz.avis.models.AvisCar;
import com.remitty.caronz.avis.models.AvisLocation;
import com.remitty.caronz.cars.adapters.MyCarPageAdapter;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.HireModel;
import com.remitty.caronz.models.OrderModel;
import com.remitty.caronz.models.RentalModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvisSearchActivity extends AppCompatActivity {
    SettingsMain settingsMain;
    RestService restService;

    LinearLayout tvEmpty;
    RecyclerView recyclerView;
    AvisCarAdapter mAdapter;
    ArrayList<AvisCar> carList = new ArrayList<AvisCar>();

    private AvisLocation pickupLocation, dropoffLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avis_search);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.tvToolbarTitle);
        title.setText(getIntent().getStringExtra("brand"));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent() != null) {
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

        tvEmpty = findViewById(R.id.empty_view);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new AvisCarAdapter(AvisSearchActivity.this, carList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setListener(new AvisCarAdapter.Listener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(AvisSearchActivity.this, AvisCarDetailActivity.class);
                intent.putExtra("car", carList.get(position));
                intent.putExtra("pickup", pickupLocation);
                intent.putExtra("dropoff", pickupLocation);
                intent.putExtra("dropoff_time", getIntent().getStringExtra("dropoff_time"));
                intent.putExtra("pickup_time", getIntent().getStringExtra("pickup_time"));
                intent.putExtra("brand", getIntent().getStringExtra("brand"));
                startActivity(intent);
            }
        });


        searchCars();
    }

    private void searchCars() {
        settingsMain.showDilog(this);

        if (SettingsMain.isConnectingToInternet(this)) {

            JsonObject params = new JsonObject();
            params.addProperty("brand", getIntent().getStringExtra("brand"));
            params.addProperty("pickup_date", getIntent().getStringExtra("pickup_time"));
            params.addProperty("pickup_location_code", pickupLocation.getCode());
            params.addProperty("dropoff_date", getIntent().getStringExtra("dropoff_time"));
            params.addProperty("dropoff_location_code", dropoffLocation.getCode());
            params.addProperty("country_code", pickupLocation.getCountry());

            Log.e("avis search params", params.toString());

            Call<ResponseBody> myCall = restService.searchAvis(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info load realty Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if(response.getBoolean("success")) {
                                carList.clear();

                                JSONArray cars = response.getJSONArray("data");
                                Log.e("activecars", cars.toString());
                                for (int i = 0; i < cars.length(); i++) {
                                    AvisCar car = new AvisCar();
                                    car.setData(cars.getJSONObject(i));
                                    carList.add(car);
                                }

                                mAdapter.notifyDataSetChanged();

                                if(carList.size() > 0)
                                    tvEmpty.setVisibility(View.GONE);

                            }
                            else{
                                Toast.makeText(getBaseContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
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
