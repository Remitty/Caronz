package com.remitty.caronz.payment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.utills.SettingsMain;
import com.jarklee.materialdatetimepicker.date.DatePickerDialog;
import com.jarklee.materialdatetimepicker.time.RadialPickerLayout;
import com.jarklee.materialdatetimepicker.time.TimePickerDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Date;

public class RentalBookingActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    FrameLayout loadingLayout;
    EditText editFrom, editTo, editGuestName, editFromTime, editToTime;
    Button btnPay;
    SettingsMain settingsMain;
    String carId, totalPrice="0", detailType, bookingId, bookFrom, bookTo, bookCustomer;
    boolean isFrom = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_booking);

        settingsMain = new SettingsMain(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra("book_id"))
            bookingId = intent.getStringExtra("book_id");
            if(intent.hasExtra("book_from"))
            bookFrom = intent.getStringExtra("book_from");
            if(intent.hasExtra("book_to"))
            bookTo = intent.getStringExtra("book_to");
            if(intent.hasExtra("book_customer"))
            bookCustomer = intent.getStringExtra("book_customer");
            if(intent.hasExtra("car_id"))
            carId = intent.getStringExtra("car_id");
        }

        initComponents();

        loadingLayout = (FrameLayout) findViewById(R.id.loadingLayout);

        initListeners();

    }

    private void initComponents() {

        editFrom = findViewById(R.id.book_from);
        if(bookFrom != null)
            editFrom.setText(bookFrom);
        editTo = findViewById(R.id.book_to);
        if(bookTo != null)
            editTo.setText(bookTo);
        editGuestName = findViewById(R.id.book_guest_name);
        if(bookCustomer != null)
            editGuestName.setText(bookCustomer);

        editFromTime = findViewById(R.id.book_from_time);
        editToTime = findViewById(R.id.book_to_time);

        btnPay = findViewById(R.id.book_pay);
    }

    private void initListeners() {
        editFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFrom = true;
                hideSoftKeyboard(v);
                showDatePickerDialog();

            }
        });

        editFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFrom = true;
                hideSoftKeyboard(v);
                showTimePickerDialog();
            }
        });

        editToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFrom = false;
                hideSoftKeyboard(v);
                showTimePickerDialog();
            }
        });

        editTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFrom = false;
                hideSoftKeyboard(v);
                showDatePickerDialog();
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validate = true;
                if(TextUtils.isEmpty(editFrom.getText())) {
                    editFrom.setError("!");
                    validate = false;
                }
                if(TextUtils.isEmpty(editTo.getText())) {
                    editTo.setError("!");
                    validate = false;
                }
//                if(TextUtils.isEmpty(editGuestName.getText())) {
//                    editGuestName.setError("!");
//                    validate = false;
//                }
                if(validate) {
                    Intent intent = new Intent(RentalBookingActivity.this, StripePayment.class);
                    intent.putExtra("id", carId);
                    intent.putExtra("from", editFrom.getText().toString());
                    intent.putExtra("from_time", editFromTime.getText().toString());
                    intent.putExtra("to", editTo.getText().toString());
                    intent.putExtra("to_time", editToTime.getText().toString());
                    intent.putExtra("service", "rent");
//                    intent.putExtra("guest", editGuestName.getText().toString());
//                    if(detailType != null) {
//                        intent.putExtra("detail_type", detailType);
//                    }
                    if(bookingId != null) {
                        intent.putExtra("booking_id", bookingId);
                    }
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getBaseContext(), "please fill in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showTimePickerDialog() {
        Date currentTime = Calendar.getInstance().getTime();
        TimePickerDialog dpd = TimePickerDialog.newInstance(
                RentalBookingActivity.this, currentTime.getHours(), currentTime.getMinutes(), true
        );
        dpd.show(this.getSupportFragmentManager(), "Timepickerdialog");
    }

    private void showDatePickerDialog() {
        Date currentTime = Calendar.getInstance().getTime();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                RentalBookingActivity.this, currentTime.getYear()+1900, currentTime.getMonth(), currentTime.getDate()
        );
        dpd.show(this.getSupportFragmentManager(), "Datepickerdialog");
    }

    private void showLoading(){
        Drawable drawable = getResources().getDrawable(R.drawable.bg_uploading).mutate();
        drawable.setColorFilter(Color.parseColor(settingsMain.getMainColor()), PorterDuff.Mode.SRC_ATOP);
        loadingLayout.setBackground(drawable);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    public void hideSoftKeyboard(View view) {
//        if (this.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
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

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if(isFrom) editFrom.setText((monthOfYear+1) + "/" + dayOfMonth + "/" + year);
        else editTo.setText((monthOfYear+1) + "/" + dayOfMonth + "/" + year);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        if(isFrom) editFromTime.setText(hourOfDay + ":" + minute);
        else editToTime.setText(hourOfDay + ":" + minute);
    }
}
