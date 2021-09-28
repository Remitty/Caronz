package com.remitty.caronz.hire;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.hire.adapter.HireAdapter;
import com.remitty.caronz.models.HireModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HireHistory extends Fragment {
    RestService restService;
    SettingsMain settingsMain;

    RecyclerView recyclerHistory;
    HireAdapter mAdapter;
    private List<HireModel> bookingRealtyList = new ArrayList<>();

    public HireHistory() {
        // Required empty public constructor
    }

    public static HireHistory newInstance(List<HireModel> history) {
        HireHistory fragment = new HireHistory();
        fragment.bookingRealtyList = history;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hire_history, container, false);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        recyclerHistory = view.findViewById(R.id.recycler_history);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        LinearLayout emptyLayout = view.findViewById(R.id.empty_view);

        if(bookingRealtyList.size() > 0) {
            recyclerHistory.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerHistory.setVisibility(View.GONE);
        }

        mAdapter = new HireAdapter(getActivity(), bookingRealtyList);
        recyclerHistory.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new HireAdapter.Listener(){
            @Override
            public void onItemEdit(int position) {
            }

            @Override
            public void onItemCancel(int position) {
            }

            @Override
            public void onItemRemove(int position) {
                removeHistory(position);
            }

            @Override
            public void onItemClick(int position) {
                HireModel item = bookingRealtyList.get(position);
                Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                intent.putExtra("carId", item.getCarId());
                startActivity(intent);
            }

            @Override
            public void onItemConfirm(int position) {

            }

        });
        mAdapter.notifyDataSetChanged();
        return view;
    }

    private void removeHistory(int position) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("id", bookingRealtyList.get(position).getId());

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.hireDelete(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info confirm book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                bookingRealtyList.remove(position);
                                mAdapter.notifyDataSetChanged();
                            }
                            else {
//                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.d("confirm error", response.getString("message"));
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
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }
}
