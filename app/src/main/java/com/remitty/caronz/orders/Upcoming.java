package com.remitty.caronz.orders;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.remitty.caronz.avis.AvisCarDetailActivity;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.models.RentalModel;
import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.orders.adapter.RentalAdapter;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Upcoming extends Fragment {
    private List<RentalModel> bookingRealtyList = new ArrayList<>();
    RecyclerView recyclerUpcoming;
    RentalAdapter mAdapter;
    RestService restService;
    SettingsMain settingsMain;
    LinearLayout emptyLayout;
    int selected;

    boolean type;

    public Upcoming() {
        // Required empty public constructor
    }

    public static Upcoming newInstance(List<RentalModel> upcoming) {
        Upcoming fragment = new Upcoming();
        fragment.bookingRealtyList = upcoming;
        return fragment;
    }

    public static Upcoming newInstance(boolean type) {
        Upcoming fragment = new Upcoming();
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
                inflater.inflate(R.layout.fragment_upcoming, container, false);
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

        mAdapter = new RentalAdapter(getActivity(), bookingRealtyList, true);
        recyclerUpcoming.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new RentalAdapter.Listener(){
            @Override
            public void onItemEdit(int position) {

            }

            @Override
            public void onItemCancel(int position) {
                selected = position;
                RentalModel item = bookingRealtyList.get(position);
                if(item.getPayment().equals("Cash") || item.getPayment().equals("Balance")  || item.getPayment().equals("Card"))
                    getCancelInfo(item.getId());
                else showCancelAlert(item.getId(), "0");

            }

            @Override
            public void onItemClick(int position) {
                RentalModel item = bookingRealtyList.get(position);
                if(item.getPayment().equals("Cash") || item.getPayment().equals("Balance")  || item.getPayment().equals("Card")) {
                    Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                    intent.putExtra("carId", item.getCarId());
                    intent.putExtra("method", "rental");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), AvisCarDetailActivity.class);
                    intent.putExtra("bookId", item.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onItemConfirm(int position) {
                selected = position;
                RentalModel item = bookingRealtyList.get(position);
                showFeedbackDialog(item.getId());
            }

        });

        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void showFeedbackDialog(String bookid) {
        final Dialog dialog = new Dialog(getActivity(), R.style.customDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_send_feedback);
        //noinspection ConstantConditions
        Button Send = dialog.findViewById(R.id.send_button);
        Button Cancel = dialog.findViewById(R.id.cancel_button);
        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        final EditText textArea_information = dialog.findViewById(R.id.textArea_information);

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                if(ratingBar.getRating() == 0) {
                    Toast.makeText(getContext(), "Please select rating bar.", Toast.LENGTH_SHORT).show();
                    check = false;
                }
                if (check) {
                    submitConfirmBook(bookid, ratingBar.getRating());
                    dialog.dismiss();
                }

            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void submitConfirmBook(String bookid, float rating) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("book_id", bookid);
            params.addProperty("rate", rating);

            Log.d("info cancel book", "" + params.toString());

            Call<ResponseBody> myCall = restService.bookingcomplete(params, UrlController.AddHeaders(getActivity()));
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

            Call<ResponseBody> myCall = restService.bookingcancelinfo(params, UrlController.AddHeaders(getActivity()));
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

            Call<ResponseBody> myCall = restService.bookingcancel(params, UrlController.AddHeaders(getActivity()));
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
                                if (bookingRealtyList.size() == 0)
                                    emptyLayout.setVisibility(View.VISIBLE);
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
