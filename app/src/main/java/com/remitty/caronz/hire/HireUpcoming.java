package com.remitty.caronz.hire;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.hire.adapter.HireAdapter;
import com.remitty.caronz.models.HireModel;
import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HireUpcoming extends Fragment {
    private List<HireModel> bookingRealtyList = new ArrayList<>();
    RecyclerView recyclerUpcoming;
    HireAdapter mAdapter;
    RestService restService;
    SettingsMain settingsMain;
    LinearLayout emptyLayout;
    int selected;

    boolean type;

    public HireUpcoming() {
        // Required empty public constructor
    }

    public static HireUpcoming newInstance(List<HireModel> upcoming) {
        HireUpcoming fragment = new HireUpcoming();
        fragment.bookingRealtyList = upcoming;
        return fragment;
    }

    public static HireUpcoming newInstance(boolean type) {
        HireUpcoming fragment = new HireUpcoming();
        fragment.type = type;
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
        View view =
                inflater.inflate(R.layout.fragment__hire_upcoming, container, false);
        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        emptyLayout = view.findViewById(R.id.empty_view);

        recyclerUpcoming = view.findViewById(R.id.recycler_upcoming);
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));

        if(bookingRealtyList.size() > 0) {
            recyclerUpcoming.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerUpcoming.setVisibility(View.GONE);
        }

        mAdapter = new HireAdapter(getActivity(), bookingRealtyList, true);
        recyclerUpcoming.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new HireAdapter.Listener(){
            @Override
            public void onItemEdit(int position) {

            }

            @Override
            public void onItemCancel(int position) {
                selected = position;
                HireModel item = bookingRealtyList.get(position);
                getCancelInfo(item.getId());

            }

            @Override
            public void onItemClick(int position) {
                HireModel item = bookingRealtyList.get(position);
                Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                intent.putExtra("carId", item.getCarId());
                intent.putExtra("method", "rental");
                startActivity(intent);
            }

            @Override
            public void onItemConfirm(int position) {
                selected = position;
                HireModel item = bookingRealtyList.get(position);
                showConfirmAlert(item.getId());
            }

        });

        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void showConfirmAlert(String bookid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Are you sure you want to to confirm this rental? ");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                submitConfirmBook(bookid);
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

    private void submitConfirmBook(String bookid) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookid);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.hireComplete(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info confirm book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                bookingRealtyList.remove(selected);
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

    private void getCancelInfo(String bookid) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

//            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookid);

            Log.d("info book cancel", "" + params.toString());

            Call<ResponseBody> myCall = restService.hireCancelInfo(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info cancel book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                boolean cancancelbook = response.getBoolean("book");
                                String refund = response.getString("refund");
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setMessage("Are you sure you want to to cancel this book? " + response.getString("message"));
                                builder.setCancelable(true);
                                if(cancancelbook) {
                                    builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showCancelAlert(bookid, refund);
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                                else{
                                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                                AlertDialog alert = builder.create();
                                alert.show();
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

    private void showCancelAlert(String bookid, String refund) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Are you sure you want to to cancel this book? ");
        builder.setCancelable(true);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestCancelBook(bookid, refund);
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

    private void requestCancelBook(String bookid, String refund) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookid);
            params.addProperty("refund", refund);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.hireCancel(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info cancel book", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if(response.getBoolean("success")) {
                                Toast.makeText(getContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                bookingRealtyList.remove(selected);
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
