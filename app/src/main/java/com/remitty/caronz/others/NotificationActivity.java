package com.remitty.caronz.others;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
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

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.adapters.NotificationAdapter;
import com.remitty.caronz.models.NotificationModel;
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
import retrofit2.http.Path;

public class NotificationActivity extends AppCompatActivity {

    SettingsMain settingsMain;
    RestService restService;

    LinearLayout tvEmpty;

    NotificationAdapter mAdapter;
    RecyclerView recyclerView;
    ArrayList<NotificationModel> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvEmpty = findViewById(R.id.empty_view);

        recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
        mAdapter = new NotificationAdapter(this, notifications);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setListener(new NotificationAdapter.Listener() {
            @Override
            public void onDelete(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(NotificationActivity.this);
                alert.setTitle("Confirm")
                        .setMessage("Are you sure you want to delete this notification")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNotification(position);
                            }
                        }).show();
            }
        });

        getNotification();

    }

    private void deleteNotification(int which) {
        if (SettingsMain.isConnectingToInternet(this)) {
            SettingsMain.showDilog(NotificationActivity.this);
            NotificationModel notification = notifications.get(which);

            Log.e("del noti id", notification.getId());
            Call<ResponseBody> myCall = restService.deleteNotification(notification.getId(), UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                        Log.d("del notifcation Resp", "" + responseObj.toString());
                    if (responseObj.isSuccessful()) {
                        notifications.remove(which);
                        mAdapter.notifyDataSetChanged();

                        if (notifications.size() == 0) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(NotificationActivity.this);
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

    private void getNotification() {
        if (SettingsMain.isConnectingToInternet(this)) {
            SettingsMain.showDilog(NotificationActivity.this);
            Call<ResponseBody> myCall = restService.notifications(UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info notification Resp", "" + responseObj.toString());
                            notifications.clear();
                            JSONObject response = new JSONObject(responseObj.body().string());
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i ++) {
                                NotificationModel notification = new NotificationModel(data.getJSONObject(i));
                                notifications.add(notification);
                            }
                            mAdapter.notifyDataSetChanged();

                            if (notifications.size() > 0) {
                                tvEmpty.setVisibility(View.GONE);
                            }
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
