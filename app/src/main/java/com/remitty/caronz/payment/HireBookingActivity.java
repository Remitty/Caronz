package com.remitty.caronz.payment;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.jarklee.materialdatetimepicker.date.DatePickerDialog;
import com.jarklee.materialdatetimepicker.time.RadialPickerLayout;
import com.jarklee.materialdatetimepicker.time.TimePickerDialog;
import com.remitty.caronz.R;
import com.remitty.caronz.gms.CustomGooglePlacesSearchActivity;
import com.remitty.caronz.helper.GMSHelper;
import com.remitty.caronz.models.PlacePredictions;

import java.util.Calendar;
import java.util.Date;

public class HireBookingActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST = 1000;
    EditText editArriveDate, editArriveTime;
    private TextView txtDestination, txtaddressSource;
    Button btnPay;

    private String carId, bookingId, pickupLocation="", dropoffLocation="", strSelected;
    private Double s_lati, s_long, d_lati, d_long;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hire_booking);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent() != null) {
            if(getIntent().hasExtra("car_id"))
                carId = getIntent().getStringExtra("car_id");
            if(getIntent().hasExtra("book_id"))
                bookingId = getIntent().getStringExtra("book_id");

        }

        txtDestination = findViewById(R.id.txtDestination);
        txtaddressSource = findViewById(R.id.txtaddressSource);
        editArriveTime = findViewById(R.id.edit_arrive_time);
        editArriveDate = findViewById(R.id.edit_arrive_date);
        btnPay = findViewById(R.id.btn_pay);

        initListeners();
    }

    private void initListeners() {
        editArriveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                showTimePickerDialog();
            }
        });

        editArriveDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(v);
                showDatePickerDialog();
            }
        });

        txtaddressSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSelected = "source";
                Intent intent2 = new Intent(HireBookingActivity.this, CustomGooglePlacesSearchActivity.class);
                intent2.putExtra("cursor", "source");
                intent2.putExtra("s_address", pickupLocation);
                intent2.putExtra("d_address", dropoffLocation);
                startActivityForResult(intent2, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
            }
        });

        txtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSelected = "destination";
                Intent intent2 = new Intent(HireBookingActivity.this, CustomGooglePlacesSearchActivity.class);
                intent2.putExtra("cursor", "destination");
                intent2.putExtra("s_address", pickupLocation);
                intent2.putExtra("d_address", dropoffLocation);
                startActivityForResult(intent2, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validate = true;
                if(TextUtils.isEmpty(editArriveTime.getText())) {
                    editArriveTime.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(editArriveDate.getText())) {
                    editArriveDate.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(txtaddressSource.getText())) {
                    Toast.makeText(getBaseContext(), "Please pickup your location", Toast.LENGTH_SHORT).show();
                    validate = false;
                }
                if(TextUtils.isEmpty(txtDestination.getText())) {
                    Toast.makeText(getBaseContext(), "Please pickup your destination", Toast.LENGTH_SHORT).show();
                    validate = false;
                }

                if(validate) {
                    Intent intent = new Intent(HireBookingActivity.this, StripePayment.class);
                    intent.putExtra("id", carId);
                    intent.putExtra("start_time", editArriveDate.getText().toString() + " " + editArriveTime.getText().toString());
                    intent.putExtra("s_address", txtaddressSource.getText().toString());
                    intent.putExtra("d_address", txtDestination.getText().toString());
                    intent.putExtra("s_latitude", s_lati+"");
                    intent.putExtra("s_longitude", s_long+"");
                    intent.putExtra("d_latitude", d_lati+"");
                    intent.putExtra("d_longitude", d_long+"");
                    intent.putExtra("service", "hire");
                    if(bookingId != null) {
                        intent.putExtra("booking_id", bookingId);
                    }
                    startActivity(intent);
                }

            }
        });
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showTimePickerDialog() {
        Date currentTime = Calendar.getInstance().getTime();
        TimePickerDialog dpd = TimePickerDialog.newInstance(
                this, currentTime.getHours(), currentTime.getMinutes(), true
        );
        dpd.show(this.getSupportFragmentManager(), "Timepickerdialog");
    }

    private void showDatePickerDialog() {
        Date currentTime = Calendar.getInstance().getTime();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this, currentTime.getYear()+1900, currentTime.getMonth(), currentTime.getDate()
        );
        dpd.show(this.getSupportFragmentManager(), "Datepickerdialog");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST) {

            if (resultCode == Activity.RESULT_OK) {

                PlacePredictions placePredictions;
                placePredictions = (PlacePredictions) data.getSerializableExtra("Location Address");
//                strPickLocation = data.getExtras().getString("pick_location");
//                strPickType = data.getExtras().getString("type");

                if (placePredictions != null) {

                        if (strSelected.equals("source")) {
                            try {
                                s_lati = Double.parseDouble(placePredictions.strSourceLatitude);
                                s_long =  Double.parseDouble(placePredictions.strSourceLongitude);
                                pickupLocation = GMSHelper.getCompleteAddressString(this, s_lati, s_long);
                                txtaddressSource.setText(pickupLocation);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            d_lati = Double.parseDouble(placePredictions.strSourceLatitude);
                            d_long = Double.parseDouble(placePredictions.strSourceLongitude);
                            dropoffLocation = GMSHelper.getCompleteAddressString(this, d_lati, d_long);
                            txtDestination.setText(dropoffLocation);

                        }

                    }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        editArriveTime.setText(hourOfDay + ":" + minute);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        editArriveDate.setText((monthOfYear+1) + "/" + dayOfMonth + "/" + year);
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


}
