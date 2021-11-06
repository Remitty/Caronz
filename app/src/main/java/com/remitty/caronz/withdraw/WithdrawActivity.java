package com.remitty.caronz.withdraw;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.helper.SharedHelper;
import com.remitty.caronz.helper.WebViewActivity;
import com.remitty.caronz.utills.PlaidConnect;
import com.google.gson.JsonObject;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

public class WithdrawActivity extends AppCompatActivity {
    private static final int STRIPE_CONNECT = 200;
    SettingsMain settingsMain;
    RestService restService;

    TextView tvBalance;
    EditText editCashAmount;
    Button btnCashout, btnConnectBank, btnHistory, btnPaypal;
    RelativeLayout loadingLayout;
    String stripeAccount;
    String selectedCard, type, alertMsg, bank="", paypal="";
    boolean stripeAccountVerified = false;
    Double balance = 0.0, fee = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setTitle("Balance Withdraw");


        initComponents();

        initListeners();

        loadAccount();
    }

    private void initComponents() {
        tvBalance = findViewById(R.id.text_balance);
        editCashAmount = findViewById(R.id.edit_cash_amount);
        btnCashout = findViewById(R.id.btn_cashout);
        btnPaypal = findViewById(R.id.btn_cashout_paypal);
        btnConnectBank = findViewById(R.id.btn_connect_bank);
        btnHistory = findViewById(R.id.btn_history);
        loadingLayout = findViewById(R.id.loadingLayout);
    }


    private void initListeners() {
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WithdrawActivity.this, WithdrawHistoryActivity.class));
            }
        });
        btnConnectBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bank.isEmpty()) {
                    if(stripeAccountVerified) {
                        new PlaidConnect(WithdrawActivity.this).openPlaid();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawActivity.this);
                        builder.setTitle(getBaseContext().getResources().getString(R.string.app_name))
                                .setIcon(R.mipmap.ic_launcher)
                                .setMessage("Would you link your bank?");
                        builder.setCancelable(true);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createStripeConnectLink(true);
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
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawActivity.this);
                    builder.setTitle(getBaseContext().getResources().getString(R.string.app_name))
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("You linked bank already. Would you link again?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new PlaidConnect(WithdrawActivity.this).openPlaid();
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

            }
        });
        btnCashout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!stripeAccountVerified) {
                    showAlertDialog("Please link your bank first.");
                    return;
                }

                if(editCashAmount.getText().toString().isEmpty()) {
                    editCashAmount.setError("!");
                    return;
                }

                if(Double.parseDouble(editCashAmount.getText().toString()) > balance) {
                    showAlertDialog("Insufficient balance");
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawActivity.this);
                builder.setTitle(getBaseContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to withdraw $ "+editCashAmount.getText().toString()+"? Fee is $" + fee + ".");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestWithdraw("Bank");
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

        btnPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editCashAmount.getText().toString().isEmpty()) {
                    editCashAmount.setError("!");
                    return;
                }

                if(Double.parseDouble(editCashAmount.getText().toString()) > balance) {
                    showAlertDialog("Insufficient balance");
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawActivity.this);
                builder.setTitle(getBaseContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to withdraw $ "+editCashAmount.getText().toString()+"? Fee is $" + fee + ".");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showInputPaypalDialog();
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
    }

    private void showInputPaypalDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_input_paypal, null);
        final EditText editPaypal = view.findViewById(R.id.edit_paypal);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        editPaypal.setText(SharedHelper.getKey(getBaseContext(), "paypal"));
        androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(WithdrawActivity.this);
        alert.setView(view);

        final androidx.appcompat.app.AlertDialog alertDialog = alert.create();
        alertDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paypal = editPaypal.getText().toString();
                Pattern p = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");
                final Matcher m = p.matcher(paypal);
                if(paypal.isEmpty()) {
                    editPaypal.setError("!");
                    return;
                }
                if(!m.matches()) {
                    editPaypal.setError("Invalid paypal");
                    return;
                }
                alertDialog.dismiss();
                SharedHelper.putKey(getBaseContext(), "paypal", paypal);
                requestWithdraw(paypal);
            }
        });
    }

    private void createStripeConnectLink(boolean create) {
        if (SettingsMain.isConnectingToInternet(this)) {
            settingsMain.showDilog(this);

            Call<ResponseBody> myCall = restService.connectStripe(UrlController.AddHeaders(getBaseContext()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info delete card Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            stripeAccountVerified = response.getBoolean("stripe_account_verified");
                            if(stripeAccountVerified) {
//                                showAlertDialog(response.getString("message"));
                                new PlaidConnect(WithdrawActivity.this).openPlaid();
                            } else {
                                if(create) {
                                    Intent intent = new Intent(WithdrawActivity.this, WebViewActivity.class);
                                    intent.putExtra("uri", response.getString("stripe_connect_link"));
                                    startActivityForResult(intent, STRIPE_CONNECT);
                                } else {
                                    showAlertDialog("Not connected.");
                                }
                            }

                        } else {
                            Log.e("stripe connect error", responseObj.errorBody().string());
                            showAlertDialog(responseObj.errorBody().string());
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
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    }
                    else {
                        Toast.makeText(getBaseContext(), "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            settingsMain.hideDilog();
            Toast.makeText(this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAccount() {
        if (SettingsMain.isConnectingToInternet(this)) {
            settingsMain.showDilog(this);

            Call<ResponseBody> myCall = restService.getAccount(UrlController.AddHeaders(getBaseContext()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    settingsMain.hideDilog();
                    try {
                        Log.d("info load account Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Log.d("info load account Resp", response.toString());
                            tvBalance.setText("$ "+response.getString("balance"));

                            paypal = response.getString("paypal");

                            balance = response.getDouble("balance");
                            fee = response.getDouble("withdraw_fee");

                            tvBalance.setText("$ "+ new DecimalFormat("#,###.##").format(balance));

                            bank = response.getString("stripe_bank");
                            stripeAccountVerified = response.getInt("stripe_account_verified") == 0? false: true;

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
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    }
                    else {
                        Toast.makeText(getBaseContext(), "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            settingsMain.hideDilog();
            Toast.makeText(this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestWithdraw(String source) {
        if (SettingsMain.isConnectingToInternet(this)) {
            loadingLayout.setVisibility(View.VISIBLE);
            JsonObject params = new JsonObject();
            params.addProperty("withdraw_price", editCashAmount.getText().toString());
            params.addProperty("source", source);

            Call<ResponseBody> myCall = restService.withdraw(params, UrlController.AddHeaders(getBaseContext()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        Log.d("info withdraw Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            settingsMain.showAlertDialog(WithdrawActivity.this, response.get("message").toString());

                        } else {
                            showAlertDialog(responseObj.errorBody().string());
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
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                    }
                    else {
                        Toast.makeText(getBaseContext(), "Something error", Toast.LENGTH_SHORT).show();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            loadingLayout.setVisibility(View.GONE);
            Toast.makeText(this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(WithdrawActivity.this);
        builder.setTitle(getBaseContext().getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == STRIPE_CONNECT) {
            createStripeConnectLink(false);
        }

        if (!new PlaidConnect(WithdrawActivity.this).myPlaidResultHandler.onActivityResult(requestCode, resultCode, data)) {
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

    @Override
    protected void onResume() {
        super.onResume();
    }
}
