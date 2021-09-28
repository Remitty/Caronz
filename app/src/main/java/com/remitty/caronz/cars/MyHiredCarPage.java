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
import com.remitty.caronz.cars.adapters.MyHiredAdapter;
import com.remitty.caronz.models.HireModel;
import com.remitty.caronz.models.HireModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import java.io.IOException;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyHiredCarPage extends Fragment {
    private ArrayList<HireModel> rentalCarList = new ArrayList<>();
    RecyclerView recyclerUpcoming;
    MyHiredAdapter mAdapter;
    LinearLayout emptyLayout;
    private boolean type = false;

    SettingsMain settingsMain;
    RestService restService;

    public MyHiredCarPage() {
        // Required empty public constructor
    }

    public static MyHiredCarPage newInstance(ArrayList<HireModel> upcoming) {
        MyHiredCarPage fragment = new MyHiredCarPage();
        fragment.rentalCarList = upcoming;
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
        View view = inflater.inflate(R.layout.fragment_upcoming_realty_page, container, false);

        recyclerUpcoming = view.findViewById(R.id.recycler_upcoming);
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new MyHiredAdapter(getActivity(), rentalCarList);
        recyclerUpcoming.setAdapter(mAdapter);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        emptyLayout = view.findViewById(R.id.empty_view);

        if(rentalCarList.size() > 0) {
            recyclerUpcoming.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerUpcoming.setVisibility(View.GONE);
        }

        mAdapter.setOnItemClickListener(new MyHiredAdapter.Listener() {

            @Override
            public void onItemCancel(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Confirm")
                        .setMessage("Are you sure you want to decline this book")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HireModel book = rentalCarList.get(position);
                                requestCancelBook(book.getId());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onItemConfirm(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Confirm")
                        .setMessage("Are you sure you want to confirm this book")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HireModel book = rentalCarList.get(position);
                                submitConfirmBook(book.getId());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onItemRemove(int position) {
                removeHistory(position);
            }


        });

        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void submitConfirmBook(String bookid) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookid);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.hireConfirm(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info confirm book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                rentalCarList.clear();
                                JSONArray rentalCars = response.getJSONArray("data");
                                Log.e("rentalcars", rentalCars.toString());
                                for (int i = 0; i < rentalCars.length(); i++) {
                                    HireModel car = new HireModel(rentalCars.getJSONObject(i));
                                    rentalCarList.add(car);
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

    private void requestCancelBook(String bookid) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookid);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.hireDecline(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info cancel book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                rentalCarList.clear();
                                JSONArray rentalCars = response.getJSONArray("data");
                                Log.e("rentalcars", rentalCars.toString());
                                for (int i = 0; i < rentalCars.length(); i++) {
                                    HireModel car = new HireModel(rentalCars.getJSONObject(i));
                                    rentalCarList.add(car);
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

    private void removeHistory(int position) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("id", rentalCarList.get(position).getId());

            Log.d("info remove book", "" + params.toString());

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
                                rentalCarList.remove(position);
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

