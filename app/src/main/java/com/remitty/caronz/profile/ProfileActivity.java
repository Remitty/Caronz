package com.remitty.caronz.profile;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.models.DocumentModel;
import com.remitty.caronz.models.UserModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    SettingsMain settingsMain;
    RestService restService;

    LinearLayout editProfBtn;
    TextView btnUploadDocument;

    TextView textViewUserName, textViewEmailvalue, textViewPhonevalue, textViewLocationvalue, tvAddress, tvAddress2, tvBalance, tvZipcode;
    ImageView imageViewProfile;
    ImageView licenseImage, registrationImage, insuranceImage, otherImage1, otherImage2;
    Dialog dialog;

    String firstName, lastName;
    private UserModel profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        textViewUserName = findViewById(R.id.text_viewName);

        editProfBtn = findViewById(R.id.edit_profile_layout);
        btnUploadDocument = findViewById(R.id.tv_uploading);

        imageViewProfile = findViewById(R.id.image_photo);

        textViewEmailvalue = findViewById(R.id.text_viewEmail);
        textViewPhonevalue = findViewById(R.id.etPhoneNumber);
        textViewLocationvalue = findViewById(R.id.tv_location);
        tvAddress = findViewById(R.id.tv_address);
        tvAddress2 = findViewById(R.id.tv_address2);
        tvBalance = findViewById(R.id.tv_balance);
        tvZipcode = findViewById(R.id.tv_zipcode);

        licenseImage = findViewById(R.id.image_license);
        registrationImage = findViewById(R.id.image_register);
        insuranceImage = findViewById(R.id.image_insurance);
        otherImage1 = findViewById(R.id.image_other1);
        otherImage2 = findViewById(R.id.image_other2);


        dialog = new Dialog(this, R.style.customDialog);

        editProfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                intent.putExtra("first_name", firstName);
                intent.putExtra("last_name", lastName);
                intent.putExtra("email", textViewEmailvalue.getText().toString());
                intent.putExtra("country_code", profile.getCountryCode());
                intent.putExtra("mobile", profile.getMobile());
                intent.putExtra("address1", tvAddress.getText().toString());
                intent.putExtra("address2", tvAddress2.getText().toString());
                intent.putExtra("zipcode", tvZipcode.getText().toString());
                intent.putExtra("location", textViewLocationvalue.getText().toString());
                intent.putExtra("photo", profile.getPicture());
                startActivity(intent);
            }
        });

        btnUploadDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, DocumentUploadActivity.class);
                startActivity(intent);
            }
        });

        setAllViewsText();


    }

    private void setAllViewsText() {

        if (SettingsMain.isConnectingToInternet(this)) {

            SettingsMain.showDilog(this);

            Call<ResponseBody> myCall = restService.getEditProfileDetails(UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Edit Profile ", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info Edit ProfileGet", "" + response.getJSONObject("data"));

                                JSONObject jsonObject = response.getJSONObject("data");

                                profile = new UserModel(jsonObject);

                                Picasso.with(getBaseContext()).load(profile.getPicture())
                                        .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(imageViewProfile);

                                firstName = profile.getFirstName();
                                lastName = profile.getLastName();

                                textViewUserName.setText(firstName + " " + lastName);
                                textViewEmailvalue.setText(profile.getEmail());

                                textViewPhonevalue.setText(profile.getPhone());
                                textViewLocationvalue.setText(profile.getLocation());
                                tvAddress.setText(profile.getFirstAddress());
                                tvAddress2.setText(profile.getSecondAddress());
                                tvZipcode.setText(profile.getPostalCode());
                                tvBalance.setText("$ " + profile.getBalance());

                                DocumentModel document = profile.getDocument();
                                if(document != null) {
                                    Picasso.with(getBaseContext()).load(document.getLicense()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(licenseImage);
                                    Picasso.with(getBaseContext()).load(document.getRegistration()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(registrationImage);
                                    Picasso.with(getBaseContext()).load(document.getInsurance()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(insuranceImage);
                                    Picasso.with(getBaseContext()).load(document.getOther1()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(otherImage1);
                                    Picasso.with(getBaseContext()).load(document.getOther2()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(otherImage2);

                                }

                            } else {
                                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Toast.makeText(getBaseContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.d("info Edit Profile error", String.valueOf(t));
                    Log.d("info Edit Profile error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
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
}
