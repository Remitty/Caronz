package com.remitty.caronz.payment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.remitty.caronz.models.CreditCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

public class StripePayment extends AppCompatActivity {

    RestService restService;
    SettingsMain settingsMain;

    FrameLayout loadingLayout;
    LinearLayout bookInvoiceLayout, hireInvoiceLayout, cardFormLayout, cardViewLayout;

    EditText editCardNumber, editCvc;
    Spinner monthSpinner, yearSpinner;

    Button btnChkOut;

    TextView tvCardNo, tvCardDate;
    TextView tvUnitPrice, tvDuration, tvTax, tvCommission, tvTotal, tvFrom, tvTo, tvArriveTime, tvPickupLocation, tvDropoffLocation, tvEstTime, tvEstDistance;
    TextView tvAddCard, tvChangeCard;
    ImageView imgCardBrand;
    CheckBox checkStoreCard;

    private Intent mIntent;

    List < CreditCard > creditCards = new ArrayList<>();
    String stringCardError, stringExpiryError, stringCVCError, stringInvalidCard;
    String totalcost;
    String packageType, stringTotalPrice;
    private String id = null, cardId = null, service;
    String from, to;
    String duration, estTime, estDistance;
    private  String detailType, bookingId;
    String cvcNo, cardNo;
    int month, year;
    private int selectedPosition = 0;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(settingsMain.getMainColor())));
        getSupportActionBar().setTitle("Check Out");

        mIntent = getIntent();
        if(mIntent != null) {

            if(getIntent().hasExtra("id"))
                id = getIntent().getStringExtra("id");
            if(getIntent().hasExtra("from"))
                from = getIntent().getStringExtra("from");
            if(getIntent().hasExtra("to"))
                to = getIntent().getStringExtra("to");
            if(getIntent().hasExtra("booking_id"))
                bookingId = getIntent().getStringExtra("booking_id");
            if(getIntent().hasExtra("service"))
                service = getIntent().getStringExtra("service");
        }

        initComponents();

        initListeners();

        getInvoiceData();

        if(mIntent != null) {

            if(mIntent.hasExtra("from"))
                tvFrom.setText(from);
            if(mIntent.hasExtra("from_time"))
                tvFrom.setText(from + " " + mIntent.getStringExtra("from_time"));
            if(mIntent.hasExtra("to"))
                tvTo.setText(to);
            if(mIntent.hasExtra("to_time"))
                tvTo.setText(to + " " + mIntent.getStringExtra("to_time"));

            if(mIntent.hasExtra("start_time"))
                tvArriveTime.setText(mIntent.getStringExtra("start_time"));
            if(mIntent.hasExtra("s_address"))
                tvPickupLocation.setText(mIntent.getStringExtra("s_address"));
            if(mIntent.hasExtra("d_address"))
                tvDropoffLocation.setText(mIntent.getStringExtra("d_address"));
        }

        loadingLayout = (FrameLayout) findViewById(R.id.loadingLayout);

        stringCardError = "Invalidate Card";
        stringExpiryError = "Invalidate Expiry";
        stringCVCError = "Invalidate CVC";
        stringInvalidCard = "Invalidate Card";

    }

    private void initListeners() {
        btnChkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.confirm_invoice, null);
                TextView co_tvUnitPrice = view.findViewById(R.id.invoice_unit_price);
                TextView co_tvDuration = view.findViewById(R.id.invoice_duration);
                TextView co_tvTax = view.findViewById(R.id.invoice_tax);
                TextView co_tvCommission = view.findViewById(R.id.invoice_commission);
                TextView co_tvTotal = view.findViewById(R.id.invoice_total);
                TextView co_tvTo = view.findViewById(R.id.invoice_to);
                TextView co_tvFrom = view.findViewById(R.id.invoice_from);
                co_tvUnitPrice.setText(tvUnitPrice.getText().toString());
                co_tvDuration.setText(tvDuration.getText().toString());
                co_tvTax.setText(tvTax.getText().toString());
                co_tvCommission.setText(tvCommission.getText().toString());
                co_tvTotal.setText(tvTotal.getText().toString());
                co_tvFrom.setText(from);
                co_tvTo.setText(to);

                AlertDialog.Builder builder = new AlertDialog.Builder(StripePayment.this);
                builder.setTitle(getBaseContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
//                        .setView(view);
                        .setMessage("Are you sure you want to checkout this payment with total cost: $ "+totalcost);
                builder.setCancelable(true);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showLoading();
                        if(editCardNumber.getText().toString().isEmpty() && creditCards.size() == 0) {
                            Toast.makeText(getBaseContext(), "Please add card.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(editCardNumber.getText().toString().isEmpty())
                            checkout();
                        else
                            createCard();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        tvAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardFormLayout.setVisibility(View.VISIBLE);
            }
        });

        tvChangeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(creditCards.size() == 1) {
                    Toast.makeText(getBaseContext(), "You have only one card.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showCardSelectDialog();
            }
        });
    }

    private void showCardSelectDialog() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item);

        final String[] cardsList = new String[creditCards.size()];

        for (int i = 0; i < creditCards.size(); i++) {
            cardsList[i] = creditCards.get(i).getCardNo();
            arrayAdapter.add(creditCards.get(i).getCardNo());
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Change Card");
        builderSingle.setSingleChoiceItems(cardsList, selectedPosition, null);

        builderSingle.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                Log.e("Items clicked===>", "" + selectedPosition);
                CreditCard creditCard = creditCards.get(selectedPosition);
                cardId = creditCard.getCardId();
                tvCardNo.setText(creditCard.getCardNo());
                tvCardDate.setText(creditCard.getExpDate());
                if(creditCard.getBrand().equals("Visa"))
                    imgCardBrand.setImageResource(R.drawable.ic_visa);
                else imgCardBrand.setImageResource(R.drawable.ic_mastercard);
            }
        });
        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    private void initComponents() {
        this.editCardNumber = (EditText) findViewById(R.id.edit_card_no);
        this.editCvc = (EditText) findViewById(R.id.edit_cvc);
        this.monthSpinner = (Spinner) findViewById(R.id.spinner_month);
        this.yearSpinner = (Spinner) findViewById(R.id.spinner_year);
        this.btnChkOut = (Button) findViewById(R.id.button4);

        tvUnitPrice = findViewById(R.id.invoice_unit_price);
        tvDuration = findViewById(R.id.invoice_duration);
        tvTax = findViewById(R.id.invoice_tax);
        tvCommission = findViewById(R.id.invoice_commission);
        tvTotal = findViewById(R.id.invoice_total);

        tvTo = findViewById(R.id.invoice_to);
        tvFrom = findViewById(R.id.invoice_from);

        tvArriveTime = findViewById(R.id.invoice_start_time);
        tvPickupLocation = findViewById(R.id.invoice_from_location);
        tvDropoffLocation = findViewById(R.id.invoice_to_location);
        tvEstTime = findViewById(R.id.invoice_est_time);
        tvEstDistance = findViewById(R.id.invoice_est_distance);


        tvCardDate = findViewById(R.id.tv_card_date);
        tvCardNo = findViewById(R.id.tv_card_no);

        imgCardBrand = findViewById(R.id.image_brand);

        tvAddCard = findViewById(R.id.tv_add_card);
        tvChangeCard = findViewById(R.id.tv_change_card);
        checkStoreCard = findViewById(R.id.check_add_card);

        bookInvoiceLayout = findViewById(R.id.book_invoice);
        hireInvoiceLayout = findViewById(R.id.hire_invoice);

        cardFormLayout = findViewById(R.id.card_layout);
        cardViewLayout = findViewById(R.id.card_view);

        if(service.equals("buy")) {
            bookInvoiceLayout.setVisibility(View.GONE);
            hireInvoiceLayout.setVisibility(View.GONE);
        } else if (service.equals("rent")) {
            hireInvoiceLayout.setVisibility(View.GONE);
        } else {
            bookInvoiceLayout.setVisibility(View.GONE);
        }


    }

    private void getInvoiceData() {
        if (SettingsMain.isConnectingToInternet(StripePayment.this)) {

            SettingsMain.showDilog(StripePayment.this);

            JsonObject params = new JsonObject();
            params.addProperty("car_id", id);
            params.addProperty("service", service);
            if(service.equals("rent")) {
                params.addProperty("from", from);
                params.addProperty("to", to);
            }
            if(service.equals("hire")) {
                if(mIntent != null) {
                    if(mIntent.hasExtra("s_latitude"))
                        params.addProperty("s_latitude", mIntent.getStringExtra("s_latitude"));
                    if(mIntent.hasExtra("d_latitude"))
                        params.addProperty("d_latitude", mIntent.getStringExtra("d_latitude"));
                    if(mIntent.hasExtra("s_longitude"))
                        params.addProperty("s_longitude", mIntent.getStringExtra("s_longitude"));
                    if(mIntent.hasExtra("d_longitude"))
                        params.addProperty("d_longitude", mIntent.getStringExtra("d_longitude"));
                }
            }
            Log.d("info Send invoice", params.toString());

            Call<ResponseBody> myCall = restService.postInvoice(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info invoice Responce", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                                Log.d("Info invoice Data", "" + response);
                            if (response.getBoolean("success")) {

                                JSONObject data = response.getJSONObject("data");

                                tvUnitPrice.setText("$ " +  String.format("%.2f", data.getDouble("unit_price")));
                                duration = data.getString("duration");

                                tvDuration.setText(duration);
                                tvTax.setText("$ " + data.getString("tax"));
                                tvCommission.setText("$ " + data.getString("commission"));
                                totalcost = data.getString("total");
                                tvTotal.setText("$ " + totalcost);

                                initCards(response.getJSONArray("cards"));

                                if(service.equals("hire")) {
                                    estDistance = data.optString("distance");
                                    estTime = data.optString("time");
                                    tvEstDistance.setText(estDistance);
                                    tvEstTime.setText(estTime);
                                }

                            } else {
                                Toast.makeText(StripePayment.this, response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showErrorDialog(responseObj.errorBody().string());
                        }
                        SettingsMain.hideDilog();

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
                    SettingsMain.hideDilog();
                    Log.d("info Send offers ", "error" + String.valueOf(t));
                    Log.d("info Send offers ", "error" + String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(StripePayment.this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void initCards(JSONArray cards) {
        CreditCard creditCard = new CreditCard();
        for(int i = 0; i < cards.length(); i ++){
            JSONObject card = null;
            try {
                card = cards.getJSONObject(i);
                creditCard.setData(card);

                creditCards.add(creditCard);
                if(creditCard.isDefault()) {
                    tvCardNo.setText(creditCard.getCardNo());
                    tvCardDate.setText(creditCard.getExpDate());
                    if(creditCard.getBrand().equals("Visa"))
                        imgCardBrand.setImageResource(R.drawable.ic_visa);
                    else imgCardBrand.setImageResource(R.drawable.ic_mastercard);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if(cards.length() == 0) {
            cardViewLayout.setVisibility(View.GONE);
            cardFormLayout.setVisibility(View.VISIBLE);
        } else {
            cardViewLayout.setVisibility(View.VISIBLE);
            cardFormLayout.setVisibility(View.GONE);
        }
    }

    private void checkout() {

        if (SettingsMain.isConnectingToInternet(StripePayment.this)) {

            JsonObject params = new JsonObject();
            params.addProperty("car_id", id);
            if(cardId != null && !cardId.isEmpty())
                params.addProperty("card_id", cardId);

            if(bookingId != null){
                params.addProperty("booking_id", bookingId);
            }

            Call<ResponseBody> myCall = null;
            switch (service) {
                case "buy":
                    myCall = restService.postBuy(params, UrlController.AddHeaders(this));
                    break;
                case "rent":
                    params.addProperty("from", from);
                    params.addProperty("to", to);
                    if(mIntent != null) {
                        if(mIntent.hasExtra("from_time"))
                            params.addProperty("from_time", mIntent.getStringExtra("from_time"));
                        if(mIntent.hasExtra("to_time"))
                            params.addProperty("to_time", mIntent.getStringExtra("to_time"));
                    }
                    myCall = restService.postBooking(params, UrlController.AddHeaders(this));
                    break;
                case "hire":
                    if(mIntent != null) {
                        if(mIntent.hasExtra("s_latitude"))
                            params.addProperty("s_latitude", mIntent.getStringExtra("s_latitude"));
                        if(mIntent.hasExtra("d_latitude"))
                            params.addProperty("d_latitude", mIntent.getStringExtra("d_latitude"));
                        if(mIntent.hasExtra("s_longitude"))
                            params.addProperty("s_longitude", mIntent.getStringExtra("s_longitude"));
                        if(mIntent.hasExtra("d_longitude"))
                            params.addProperty("d_longitude", mIntent.getStringExtra("d_longitude"));
                        if(mIntent.hasExtra("s_address"))
                            params.addProperty("s_address", mIntent.getStringExtra("s_address"));
                        if(mIntent.hasExtra("d_address"))
                            params.addProperty("d_address", mIntent.getStringExtra("d_address"));
                        if(mIntent.hasExtra("start_time"))
                            params.addProperty("start_time", mIntent.getStringExtra("start_time"));
                        params.addProperty("est_time", estTime);
                        params.addProperty("est_distance", estDistance);
                        params.addProperty("duration", duration);
                    }
                    myCall = restService.postHiring(params, UrlController.AddHeaders(this));
                    break;

            }

            Log.d("info Checkout params", "" + params.toString());

            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                            Log.d("info Checkout Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Log.d("info Checkout object", "" + response.toString());
                            if (response.getBoolean("success")) {
                                settingsMain.setPaymentCompletedMessage(response.get("message").toString());
                                thankYou();

                            } else
                                Toast.makeText(StripePayment.this, response.get("message").toString(), Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        Toast.makeText(getApplicationContext(), "Something error", Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            loadingLayout.setVisibility(View.GONE);
            SettingsMain.hideDilog();
            Toast.makeText(StripePayment.this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }

    private void createCard() {

        cvcNo = editCvc.getText().toString();
        cardNo = editCardNumber.getText().toString();
        month = getInteger(monthSpinner);
        year = getInteger(yearSpinner);

        if (SettingsMain.isConnectingToInternet(StripePayment.this)) {

            JsonObject params = new JsonObject();
            params.addProperty("no", cardNo);
            params.addProperty("cvc", cvcNo);
            params.addProperty("month", month);
            params.addProperty("year", year);
            params.addProperty("is_store_card", checkStoreCard.isChecked());

            Call<ResponseBody> myCall = restService.createCard(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("create card Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Log.d("create card object", "" + response.toString());
                            if (response.getBoolean("success")) {
                                cardId = response.getString("card_id");
                                checkout();

                            } else
                                Toast.makeText(StripePayment.this, response.get("message").toString(), Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        Toast.makeText(getApplicationContext(), "Something error", Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            loadingLayout.setVisibility(View.GONE);
            SettingsMain.hideDilog();
            Toast.makeText(StripePayment.this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }

    }

    private Integer getInteger(Spinner spinner) {
        try {
            return Integer.parseInt(spinner.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void handleError(String error) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(settingsMain.getAlertDialogTitle("error"));
        alert.setMessage(error);
        alert.setPositiveButton(settingsMain.getAlertOkText(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void thankYou() {
        Intent intent = new Intent(StripePayment.this, Thankyou.class);
//                                intent.putExtra("data", responseData.getString("data"));
        intent.putExtra("order_thankyou_title", "Congratulation");
        intent.putExtra("order_thankyou_btn", "Home");
        startActivity(intent);
//                                SettingsMain.hideDilog();
        StripePayment.this.finish();
//        if (SettingsMain.isConnectingToInternet(StripePayment.this)) {
//            Call<ResponseBody> myCall = restService.getPaymentCompleteData(UrlController.AddHeaders(StripePayment.this));
//            myCall.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
//                    try {
//                        if (responseObj.isSuccessful()) {
//                            Log.d("info ThankYou Details", "" + responseObj.toString());
//
//                            JSONObject response = new JSONObject(responseObj.body().string());
//                            if (response.getBoolean("success")) {
//                                JSONObject responseData = response.getJSONObject("data");
//
//                                Log.d("info ThankYou object", "" + response.getJSONObject("data"));
//
//                                Intent intent = new Intent(StripePayment.this, Thankyou.class);
//                                intent.putExtra("data", responseData.getString("data"));
//                                intent.putExtra("order_thankyou_title", responseData.getString("order_thankyou_title"));
//                                intent.putExtra("order_thankyou_btn", responseData.getString("order_thankyou_btn"));
//                                startActivity(intent);
//                                SettingsMain.hideDilog();
//                                StripePayment.this.finish();
//                            } else {
//                                SettingsMain.hideDilog();
//                                Toast.makeText(StripePayment.this, response.get("message").toString(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        SettingsMain.hideDilog();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        SettingsMain.hideDilog();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    SettingsMain.hideDilog();
//                    Log.d("info ThankYou error", String.valueOf(t));
//                    Log.d("info ThankYou error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
//                }
//            });
//        } else {
//            SettingsMain.hideDilog();
//            Toast.makeText(StripePayment.this, "Internet error", Toast.LENGTH_SHORT).show();
//        }
    }

    private void showLoading(){
        Drawable drawable = getResources().getDrawable(R.drawable.bg_uploading).mutate();
        drawable.setColorFilter(Color.parseColor(SettingsMain.getMainColor()), PorterDuff.Mode.SRC_ATOP);
        loadingLayout.setBackground(drawable);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    private void showErrorDialog(String errorMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StripePayment.this);
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

