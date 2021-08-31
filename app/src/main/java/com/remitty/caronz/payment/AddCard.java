package com.remitty.caronz.payment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import androidx.fragment.app.Fragment;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCard extends Fragment {
    SettingsMain settingsMain;
    FrameLayout loadingLayout;
    RestService restService;

    EditText editCardNumber, editCvc;
    Spinner monthSpinner, yearSpinner;
    
    String cvcNo, cardNo;
    String stringCardError, stringExpiryError, stringCVCError, stringInvalidCard;
    int month=0, year=0;
    private String PUBLISHABLE_KEY;

    Button btnAddCard;

    public AddCard() {
        // Required empty public constructor
    }

    public static AddCard newInstance() {
        AddCard fragment = new AddCard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        settingsMain = new SettingsMain(getContext());
        PUBLISHABLE_KEY = settingsMain.getKey("stripeKey");

        loadingLayout = (FrameLayout) view.findViewById(R.id.loadingLayout);

        btnAddCard = view.findViewById(R.id.btn_add_card);

        this.editCardNumber = (EditText) view.findViewById(R.id.edit_card_no);
        this.editCvc = (EditText) view.findViewById(R.id.edit_cvc);
        this.monthSpinner = (Spinner) view.findViewById(R.id.spinner_month);
        this.yearSpinner = (Spinner) view.findViewById(R.id.spinner_year);

//        btnAddCard.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getContext());

        btnAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editCardNumber.getText().toString().isEmpty()) {
                    editCardNumber.setError("!");
                    return;
                }
                if(editCvc.getText().toString().isEmpty()) {
                    editCvc.setError("!");
                    return;
                }
                if(month == 0 || year == 0) {
                    Toast.makeText(getContext(), "Please select expire date.", Toast.LENGTH_SHORT).show();
                    return;
                }
                createCard();
            }
        });

        stringCardError = "Invalidate Card";
        stringExpiryError = "Invalidate Expiry";
        stringCVCError = "Invalidate CVC";
        stringInvalidCard = "Invalidate Card";

        return view;
    }



    private void createCard() {

        cvcNo = editCvc.getText().toString();
        cardNo = editCardNumber.getText().toString();
        month = getInteger(monthSpinner);
        year = getInteger(yearSpinner);

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            JsonObject params = new JsonObject();
            params.addProperty("no", cardNo);
            params.addProperty("cvc", cvcNo);
            params.addProperty("month", month);
            params.addProperty("year", year);
            params.addProperty("is_store_card", true);

            Call<ResponseBody> myCall = restService.createCard(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("create card Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Log.d("create card object", "" + response.toString());
                            
                        } else {
                            showErrorDialog(responseObj.errorBody().string());
                        }
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingLayout.setVisibility(View.GONE);
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        Toast.makeText(getContext(), "Something error", Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            loadingLayout.setVisibility(View.GONE);
            SettingsMain.hideDilog();
            Toast.makeText(getContext(), settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }

    }

    private Integer getInteger(Spinner spinner) {
        try {
            return Integer.parseInt(spinner.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private void showLoading(){
        Drawable drawable = getResources().getDrawable(R.drawable.bg_uploading).mutate();
        drawable.setColorFilter(Color.parseColor(SettingsMain.getMainColor()), PorterDuff.Mode.SRC_ATOP);
        loadingLayout.setBackground(drawable);
        loadingLayout.setVisibility(View.VISIBLE);
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

    private void showErrorDialog(String errorMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Error")
                .setIcon(R.mipmap.ic_launcher)
//                        .setView(view);
                .setMessage(errorMsg);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
