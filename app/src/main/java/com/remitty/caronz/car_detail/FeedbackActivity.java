package com.remitty.caronz.car_detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.adapters.FeedbackAdapter;
import com.remitty.caronz.models.CarFeedback;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {

    SettingsMain settingsMain;
    RestService restService;

    RecyclerView mCommentsRecyclerView;
    FeedbackAdapter feedbackAdapter;
    ArrayList<CarFeedback> commentsList = new ArrayList<>();

    TextView emptyView;

    String carId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        settingsMain = new SettingsMain(getBaseContext());
        restService = UrlController.createService(RestService.class);

        emptyView = findViewById(R.id.empty_view);

        mCommentsRecyclerView = findViewById(R.id.rating_recycler_view);
        mCommentsRecyclerView.setHasFixedSize(true);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        feedbackAdapter = new FeedbackAdapter(this, commentsList);
        mCommentsRecyclerView.setAdapter(feedbackAdapter);

        if(getIntent() != null) {
            carId = getIntent().getStringExtra("id");
        }

        getAllData(carId);
    }

    private void getAllData(final String myId) {
        if (SettingsMain.isConnectingToInternet(getBaseContext())) {

            SettingsMain.showDilog(FeedbackActivity.this);

            JsonObject params = new JsonObject();
            params.addProperty("car_id", myId);

            Call<ResponseBody> myCall = restService.getAdsFeedback(params, UrlController.AddHeaders(getBaseContext()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info feedback Respon", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                Log.d("info feedback object", "" + response.getJSONArray("data"));
                                JSONArray feedbacks = response.getJSONArray("data");
                                commentsList.clear();
                                for (int i = 0; i < feedbacks.length(); i ++) {
                                    CarFeedback feedback = new CarFeedback(feedbacks.getJSONObject(i));
                                    commentsList.add(feedback);
                                }

                                if(commentsList.size() > 0) emptyView.setVisibility(View.GONE);

                                feedbackAdapter.notifyDataSetChanged();


                            } else {
                                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                    SettingsMain.hideDilog();
                    Log.d("info AdDetails error", String.valueOf(t));
                    Log.d("info AdDetails error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getBaseContext(), "Internet error", Toast.LENGTH_SHORT).show();
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
