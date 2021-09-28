package com.remitty.caronz.others;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class NotificationFragment extends Fragment {

    SettingsMain settingsMain;
    RestService restService;

    TextView tvEmpty;

    NotificationAdapter mAdapter;
    RecyclerView recyclerView;
    ArrayList<NotificationModel> notifications = new ArrayList<>();
    
    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
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
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class);

        getActivity().setTitle("Notification");
        
        tvEmpty = view.findViewById(R.id.tv_empty);

        recyclerView = view.findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new NotificationAdapter(getActivity(), notifications);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setListener(new NotificationAdapter.Listener() {
            @Override
            public void onDelete(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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

        return view;
    }

    private void deleteNotification(int which) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {
            SettingsMain.showDilog(getActivity());
            NotificationModel notification = notifications.get(which);

            Log.e("del noti id", notification.getId());
            Call<ResponseBody> myCall = restService.deleteNotification(notification.getId(), UrlController.AddHeaders(getActivity()));
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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
                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNotification() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {
            SettingsMain.showDilog(getActivity());
            Call<ResponseBody> myCall = restService.notifications(UrlController.AddHeaders(getActivity()));
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
                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }
}
