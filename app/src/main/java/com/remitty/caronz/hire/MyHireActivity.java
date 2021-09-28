package com.remitty.caronz.hire;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.remitty.caronz.hire.adapter.HirePagerAdapter;
import com.remitty.caronz.models.HireModel;
import com.google.android.material.tabs.TabLayout;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

public class MyHireActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    SettingsMain settingsMain;
    RestService restService;

    private List<HireModel> HireUpcomingRealtyList = new ArrayList<>();
    private List<HireModel> HireHistoryRealtyList = new ArrayList<>();

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_hire);

        settingsMain = new SettingsMain(this);

        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("My Hire");
        }

        mViewPager = (ViewPager) findViewById(R.id.container);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

//        if (getIntent().getBooleanExtra("receive", false))
            mViewPager.setCurrentItem(0);

//        RentalPagerAdapter mSectionsPagerAdapter = new RentalPagerAdapter(getSupportFragmentManager(), HireUpcomingRealtyList, HireHistoryRealtyList);
//        mViewPager.setAdapter(mSectionsPagerAdapter);

        loadAllHire();
    }

    private void loadAllHire() {
        settingsMain.showDilog(this);

        if (SettingsMain.isConnectingToInternet(this)) {

            Call<ResponseBody> myCall = restService.hireList( UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info load upcoming Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if(response.getBoolean("success")) {
                                JSONArray data = response.getJSONArray("data");
                                HireUpcomingRealtyList.clear();
                                HireHistoryRealtyList.clear();

                                Log.d("info load Hirelist", data.toString());

                                if (data != null && data.length() > 0) {
                                    for (int i = 0; i < data.length(); i++) {
                                        HireModel item = new HireModel();

                                        JSONObject realty = data.getJSONObject(i);

                                        item.setData(realty);

                                        if(item.getBookStatus().equals("Hired") || item.getBookStatus().equals("Confirmed"))
                                            HireUpcomingRealtyList.add(item);
                                        else
                                            HireHistoryRealtyList.add(item);
                                    }
                                }
                                HirePagerAdapter mSectionsPagerAdapter = new HirePagerAdapter(getSupportFragmentManager(), HireUpcomingRealtyList, HireHistoryRealtyList);
                                mViewPager.setAdapter(mSectionsPagerAdapter);
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
