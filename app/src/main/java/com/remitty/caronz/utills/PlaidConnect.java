package com.remitty.caronz.utills;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.plaid.link.BuildConfig;
import com.plaid.link.Plaid;
import com.plaid.link.PlaidHandler;
import com.plaid.link.configuration.LinkLogLevel;
import com.plaid.link.configuration.LinkTokenConfiguration;
import com.plaid.link.result.LinkResultHandler;
import com.remitty.caronz.utills.Network.RestService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeoutException;

import kotlin.Unit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaidConnect {
    private Activity mContext;
    RestService restService;
    SettingsMain settingsMain;
    
    String linkToken;

    public LinkResultHandler myPlaidResultHandler = new LinkResultHandler(
            linkSuccess -> {
                sendPublicToken(linkSuccess.getPublicToken(), linkSuccess.getMetadata().getAccounts().get(0).getId());

                return Unit.INSTANCE;
            },
            linkExit -> {
                if (linkExit.getError() != null) {
                    showAlert(linkExit.getError().getDisplayMessage());
                } else {
                    showAlert(linkExit.getMetadata().getStatus() != null ? linkExit.getMetadata()
                            .getStatus()
                            .getJsonValue() : "unknown");
                }
                return Unit.INSTANCE;
            }
    );

    public PlaidConnect(Activity context){
        mContext = context;
        settingsMain = new SettingsMain(context);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), context);
    }

    public void openPlaid() {
        setOptionalEventListener();
        getLinkTokenFromServer();
    }

    public static void initPlaid(Activity context){
        new PlaidConnect(context);
    }

    /**
     * Optional, set an <a href="https://plaid.com/docs/link/android/#handling-onevent">event listener</a>.
     */
    private void setOptionalEventListener() {
        Plaid.setLinkEventListener(linkEvent -> {
            Log.i("Plaid Event", linkEvent.toString());
            return Unit.INSTANCE;
        });
    }

    /**
     * For all Link configuration options, have a look at the
     * <a href="https://plaid.com/docs/link/android/#parameter-reference">parameter reference</>
     */
    private void openLink() {
        LinkLogLevel logLevel = BuildConfig.DEBUG ? LinkLogLevel.VERBOSE : LinkLogLevel.ERROR;

        Plaid.create(
                mContext.getApplication(),
                new LinkTokenConfiguration.Builder()
                        .token(linkToken)
                        .logLevel(logLevel)
                        .build()
        ).open(mContext);
    }

    private void getLinkTokenFromServer() {
        if (SettingsMain.isConnectingToInternet(mContext)) {
            settingsMain.showDilog(mContext);

            Call<ResponseBody> myCall = restService.getPlaidLinkToken(UrlController.AddHeaders(mContext));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info plaid link token", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Log.d("info plaid link token", response.toString());
                            try {
                                linkToken = response.getString("link_token");
                                openLink();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            showAlert(responseObj.errorBody().string());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    settingsMain.hideDilog();
                    if (t instanceof TimeoutException) {
                        Toast.makeText(mContext, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(mContext, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    }
                    else {
                        Toast.makeText(mContext, "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            settingsMain.hideDilog();
            Toast.makeText(mContext, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPublicToken(String publicToken, String accountId) {
        Log.d("publictoken", publicToken);
        if (SettingsMain.isConnectingToInternet(mContext)) {
            settingsMain.showDilog(mContext);

            JsonObject params = new JsonObject();
            params.addProperty("pub_token", publicToken);
            params.addProperty("account_id", accountId);

            Call<ResponseBody> myCall = restService.postPlaidCreateBank(params, UrlController.AddHeaders(mContext));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info crate bank", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            showAlert(response.optString("message"));

                        } else {
                            showAlert(responseObj.errorBody().string());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    settingsMain.hideDilog();
                    if (t instanceof TimeoutException) {
                        Toast.makeText(mContext, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(mContext, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    }
                    else {
                        Toast.makeText(mContext, "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            settingsMain.hideDilog();
            Toast.makeText(mContext, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Alert")
//                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .show();
    }

}
