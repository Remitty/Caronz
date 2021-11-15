package com.remitty.caronz.hire;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    SettingsMain settingsMain;
    RestService restService;

    private List<HireModel> HireUpcomingRealtyList = new ArrayList<>();
    private List<HireModel> HireHistoryRealtyList = new ArrayList<>();

    LinearLayout upcomingActiveLayout, historyActiveLayout;
    TextView btnUpcoming, btnHistory;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        upcomingActiveLayout = findViewById(R.id.upcoming_active);
        historyActiveLayout = findViewById(R.id.history_active);
        historyActiveLayout.setVisibility(View.GONE);

        btnUpcoming = findViewById(R.id.btn_upcoming);
        btnHistory = findViewById(R.id.btn_history);

        mViewPager = (ViewPager) findViewById(R.id.container);
//        if (getIntent().getBooleanExtra("receive", false))
            mViewPager.setCurrentItem(0);
        initListeners();


//        RentalPagerAdapter mSectionsPagerAdapter = new RentalPagerAdapter(getSupportFragmentManager(), HireUpcomingRealtyList, HireHistoryRealtyList);
//        mViewPager.setAdapter(mSectionsPagerAdapter);

        loadAllHire();
    }

    private void initListeners() {
        btnUpcoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyActiveLayout.setVisibility(View.GONE);
                upcomingActiveLayout.setVisibility(View.VISIBLE);
                mViewPager.setCurrentItem(0);
            }
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upcomingActiveLayout.setVisibility(View.GONE);
                historyActiveLayout.setVisibility(View.VISIBLE);
                mViewPager.setCurrentItem(1);
            }
        });
        mViewPager.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int index = mViewPager.getCurrentItem();
                switch (index) {
                    case 0:
                        historyActiveLayout.setVisibility(View.GONE);
                        upcomingActiveLayout.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        upcomingActiveLayout.setVisibility(View.GONE);
                        historyActiveLayout.setVisibility(View.VISIBLE);
                        break;
                }
                return true;
            }
        });
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
