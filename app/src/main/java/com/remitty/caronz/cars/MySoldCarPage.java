package com.remitty.caronz.cars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.cars.adapters.OrderAdapter;
import com.remitty.caronz.models.OrderModel;
import com.remitty.caronz.models.OrderModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.FtsOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MySoldCarPage extends Fragment {
    SettingsMain settingsMain;
    RestService restService;

    RecyclerView recyclerRental;
    OrderAdapter mAdapter;
    private ArrayList<OrderModel> orderList = new ArrayList<>();

    public MySoldCarPage() {
        // Required empty public constructor
    }

    public static MySoldCarPage newInstance(ArrayList<OrderModel> rental) {
        MySoldCarPage fragment = new MySoldCarPage();
        fragment.orderList = rental;
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
        View view = inflater.inflate(R.layout.fragment_rental_page, container, false);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        recyclerRental = view.findViewById(R.id.recycler_rental);
        recyclerRental.setLayoutManager(new LinearLayoutManager(getContext()));

        LinearLayout emptyLayout = view.findViewById(R.id.empty_view);

        if(orderList.size() > 0) {
            recyclerRental.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerRental.setVisibility(View.GONE);
        }

        mAdapter = new OrderAdapter(getActivity(), orderList);
        recyclerRental.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OrderAdapter.Listener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onConfirm(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to confirm this order?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                        submitConfirm(orderList.get(position).getId());
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            public void onActive(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to activate this car again?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestActivate(orderList.get(position).getCarId());
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void requestActivate(String carId) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("car_id", carId);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.activateCar(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info confirm book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                orderList.clear();
                                JSONArray rentalCars = response.getJSONArray("data");
                                Log.e("ordercars", rentalCars.toString());
                                for (int i = 0; i < rentalCars.length(); i++) {
                                    OrderModel car = new OrderModel(rentalCars.getJSONObject(i));
                                    orderList.add(car);
                                }
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

    private void submitConfirm(String bookid) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("order_id", bookid);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.confirmBuy(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info cancel book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                orderList.clear();
                                JSONArray rentalCars = response.getJSONArray("data");
                                Log.e("ordercars", rentalCars.toString());
                                for (int i = 0; i < rentalCars.length(); i++) {
                                    OrderModel car = new OrderModel(rentalCars.getJSONObject(i));
                                    orderList.add(car);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                            else {
//                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                Log.d("cancel error", response.getString("message"));
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
