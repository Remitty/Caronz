package com.remitty.caronz.coins;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.coins.adapters.CoinAdapter;
import com.remitty.caronz.models.CoinModel;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinActivity extends AppCompatActivity {

    SettingsMain settingsMain;
    RestService restService;

    TextView tvEmpty, tvBalance, tvViewActivities;

    CoinAdapter mAdapter;
    RecyclerView recyclerView;
    ArrayList<CoinModel> coins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), CoinActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvEmpty = findViewById(R.id.tv_empty);
        tvBalance = findViewById(R.id.balance);
        tvViewActivities = findViewById(R.id.tv_view_activities);

        recyclerView = findViewById(R.id.recycler_coins);
        recyclerView.setLayoutManager(new LinearLayoutManager(CoinActivity.this));
        mAdapter = new CoinAdapter(CoinActivity.this, coins);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setListener(new CoinAdapter.Listener() {
            @Override
            public void onDeposit(int position) {
                depositCoin(position);
            }
        });

        tvViewActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CoinActivity.this, CoinHistoryActivity.class));
            }
        });

        getCoinList();
    }

    private void depositCoin(int which) {
        if (SettingsMain.isConnectingToInternet(this)) {
//            SettingsMain.showDilog(CoinActivity.this);
            CoinModel coin = coins.get(which);
            JsonObject params = new JsonObject();
            params.addProperty("coin", coin.getId());
            Log.e("deposit params", params.toString());
            Call<ResponseBody> myCall = restService.deposit(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    Log.d("deposit coin Resp", "" + responseObj.toString());
                    if (responseObj.isSuccessful()) {
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseObj.body().string());
                            showWalletAddressDialog(response, coin.getSymbol());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(CoinActivity.this);
                        try {
                            alert.setTitle("Error!")
                                    .setMessage(responseObj.errorBody().string())
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setPositiveButton("Yes", null)
                                    .show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info FireBase ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FireBase err", String.valueOf(t));
                        Log.d("info FireBase err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCoinList() {
        if (SettingsMain.isConnectingToInternet(this)) {
            SettingsMain.showDilog(CoinActivity.this);
            Call<ResponseBody> myCall = restService.getCoins(UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info Coin Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {
                            coins.clear();
                            JSONObject response = new JSONObject(responseObj.body().string());
                            JSONArray data = response.getJSONArray("coins");
                            Log.d("info Coin Resp", "" + data.toString());
                            for (int i = 0; i < data.length(); i ++) {
                                CoinModel Coin = new CoinModel(data.getJSONObject(i));
                                coins.add(Coin);
                            }
                            mAdapter.notifyDataSetChanged();

                            tvBalance.setText(String.format("$ %.2f", response.getDouble("wallet_balance")));

                            if (coins.size() > 0) {
                                tvEmpty.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e("coins issue", responseObj.errorBody().string());

                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info FireBase ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FireBase err", String.valueOf(t));
                        Log.d("info FireBase err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWalletAddressDialog(JSONObject data, String symbol) {

        DepositDialog mContentDialog;
        mContentDialog = new DepositDialog(R.layout.dialog_coin_deposit, data, symbol);
        mContentDialog.setListener(new DepositDialog.Listener() {

            @Override
            public void onOk() {
                Toast.makeText(getBaseContext(), "Copied successfully", Toast.LENGTH_SHORT).show();
            }

        });
        try {
            mContentDialog.show(getSupportFragmentManager(), "deposit");
        }catch (IllegalStateException e) {
            Toast.makeText(getBaseContext(), "Please try again", Toast.LENGTH_SHORT).show();
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
