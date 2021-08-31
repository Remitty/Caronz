package com.remitty.caronz.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.models.CreditCard;
import com.remitty.caronz.payment.adapter.CardListAdapter;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardListPage extends Fragment {
    SettingsMain settingsMain;
    FrameLayout loadingLayout;
    RestService restService;

    RecyclerView cardListView;
    CardListAdapter mAdapter;
    List<CreditCard> cardList = new ArrayList<>();

    public CardListPage() {
        // Required empty public constructor
    }

    public static CardListPage newInstance(List<CreditCard> cardList) {
        CardListPage fragment = new CardListPage();
        fragment.cardList = cardList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_list_page, container, false);

        settingsMain = new SettingsMain(getContext());

        loadingLayout = (FrameLayout) view.findViewById(R.id.loadingLayout);

        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getContext());

        cardListView = view.findViewById(R.id.card_list_view);

        LinearLayout emptyLayout = view.findViewById(R.id.empty_view);
        if (cardList.size() > 0) {
            emptyLayout.setVisibility(View.GONE);
            cardListView.setVisibility(View.VISIBLE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            cardListView.setVisibility(View.GONE);
        }

        cardListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CardListAdapter(cardList, true);
        mAdapter.setListener(new CardListAdapter.Listener() {

            @Override
            public void OnDelete(int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to delete this card?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CreditCard card = cardList.get(position);
                        sendCardDeleteRequest(card.getCardId(), position);
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
        cardListView.setAdapter(mAdapter);

        return view;
    }



    private void sendCardDeleteRequest(String card_id, int position) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {
            showLoading();
            JsonObject params = new JsonObject();
            params.addProperty("card_id", card_id);

            Call<ResponseBody> myCall = restService.deleteCard(params, UrlController.AddHeaders(getContext()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        Log.d("info delete card Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            Toast.makeText(getContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            cardList.remove(position);
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingLayout.setVisibility(View.GONE);
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    }
                    else {
                        Toast.makeText(getContext(), "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
//            loadingLayout.setVisibility(View.GONE);
            Toast.makeText(getActivity(), settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(){
        Drawable drawable = getResources().getDrawable(R.drawable.bg_uploading).mutate();
        drawable.setColorFilter(Color.parseColor(SettingsMain.getMainColor()), PorterDuff.Mode.SRC_ATOP);
//        loadingLayout.setBackground(drawable);
//        loadingLayout.setVisibility(View.VISIBLE);
    }

    private void handleError(String error) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle(settingsMain.getAlertDialogTitle("error"));
        alert.setMessage(error);
        alert.setPositiveButton(settingsMain.getAlertOkText(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
