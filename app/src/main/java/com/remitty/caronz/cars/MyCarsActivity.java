package com.remitty.caronz.cars;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.remitty.caronz.R;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.cars.adapters.MyCarPageAdapter;
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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

public class MyCarsActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    SettingsMain settingsMain;
    RestService restService;

    private ArrayList<CarModel> myActiveCarList = new ArrayList<>();
    private ArrayList<RentalModel> myRentalCarList = new ArrayList<>();
    private ArrayList<OrderModel> mySoldCarList = new ArrayList<>();
    private ArrayList<HireModel> myHireCarList = new ArrayList<>();

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_realty);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor(getMainColor()));
        toolbar.setTitle("My Cars");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        loadAllData();
    }

    private void loadAllData() {
        settingsMain.showDilog(this);

        if (SettingsMain.isConnectingToInternet(this)) {

            Call<ResponseBody> myCall = restService.sellerCarList( UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info load realty Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if(response.getBoolean("success")) {
                                myActiveCarList.clear();
                                myRentalCarList.clear();
                                mySoldCarList.clear();
                                myHireCarList.clear();

                                JSONArray activeCars = response.getJSONArray("active");
                                Log.e("activecars", activeCars.toString());
                                for (int i = 0; i < activeCars.length(); i++) {
                                    CarModel car = new CarModel(activeCars.getJSONObject(i));
                                        myActiveCarList.add(car);
                                }

                                JSONArray rentalCars = response.getJSONArray("rentals");
                                Log.e("rentalcars", rentalCars.toString());
                                for (int i = 0; i < rentalCars.length(); i++) {
                                    RentalModel car = new RentalModel(rentalCars.getJSONObject(i));
                                    myRentalCarList.add(car);
                                }

                                JSONArray orderCars = response.getJSONArray("orders");
                                Log.e("ordercars", orderCars.toString());
                                for (int i = 0; i < orderCars.length(); i++) {
                                    OrderModel car = new OrderModel(orderCars.getJSONObject(i));
                                    mySoldCarList.add(car);
                                }

                                JSONArray hireCars = response.getJSONArray("hires");
                                Log.e("hirecars", hireCars.toString());
                                for (int i = 0; i < hireCars.length(); i++) {
                                    HireModel car = new HireModel(hireCars.getJSONObject(i));
                                    myHireCarList.add(car);
                                }

                                mViewPager = (ViewPager) findViewById(R.id.container);
                                MyCarPageAdapter mSectionsPagerAdapter = new MyCarPageAdapter(getSupportFragmentManager(), myActiveCarList, myRentalCarList, myHireCarList, mySoldCarList);
                                mViewPager.setAdapter(mSectionsPagerAdapter);
                                tabLayout = (TabLayout) findViewById(R.id.tabs);
                                tabLayout.setupWithViewPager(mViewPager);
                                mViewPager.setCurrentItem(0);

                            }
                            else{
                                Toast.makeText(getBaseContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
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

    @Override
    protected void onResume() {
        super.onResume();
    }
}
