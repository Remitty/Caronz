package com.remitty.caronz.profile;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.JsonObject;
import com.phonenumberui.CountryCodeActivity;
import com.phonenumberui.countrycode.Country;
import com.phonenumberui.countrycode.CountryUtils;
import com.phonenumberui.utility.Utility;
import com.remitty.caronz.R;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity implements RuntimePermissionHelper.permissionInterface, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, AdapterView.OnItemClickListener{
    SettingsMain settingsMain;
    RestService restService;
    EditText editTextFirstName, editTextLastName, editTextEmail, editTextAddress1, editTextAddress2, editTextPostalCode;
    ImageView imageViewProfile;
    Button btnSave;
    TextView changePW;

    private AppCompatEditText etCountryCode;
    private AppCompatEditText etPhoneNumber;
    private ImageView imgFlag;
    private Country mSelectedCountry;
    private static final int COUNTRYCODE_ACTION = 10001;
    private PhoneNumberUtil mPhoneUtil;

    AutoCompleteTextView mLocationAutoTextView;
    private PlacesClient placesClient;
    RuntimePermissionHelper runtimePermissionHelper;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private int choosenImage;

    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();
    private String userChoosenTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

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

        runtimePermissionHelper = new RuntimePermissionHelper(this, this);
        placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        initComponents();
        initListeners();

        setPhoneUI();

        
    }

    private void initComponents() {
        imageViewProfile = findViewById(R.id.image_photo);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);

        editTextPostalCode =  findViewById(R.id.editTextPostalCode);
        editTextAddress1 =  findViewById(R.id.editTextAddress1);
        editTextAddress2 =  findViewById(R.id.editTextAddress2);

        etCountryCode =  findViewById(R.id.etCountryCode);
        etPhoneNumber =  findViewById(R.id.etPhoneNumber);
        imgFlag =  findViewById(com.phonenumberui.R.id.flag_imv);

        mLocationAutoTextView = findViewById(R.id.editTextLocation);

        if(getIntent() != null) {
            Intent intent = getIntent();
            Picasso.with(getBaseContext()).load(intent.getStringExtra("photo"))
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(imageViewProfile);
            editTextFirstName.setText(intent.getStringExtra("first_name"));
            editTextLastName.setText(intent.getStringExtra("last_name"));
            editTextEmail.setText(intent.getStringExtra("email"));
            editTextPostalCode.setText(intent.getStringExtra("zipcode"));
            editTextAddress1.setText(intent.getStringExtra("address1"));
            editTextAddress2.setText(intent.getStringExtra("address2"));
            etCountryCode.setText(intent.getStringExtra("country_code"));
            etPhoneNumber.setText(intent.getStringExtra("mobile"));
            mLocationAutoTextView.setText(intent.getStringExtra("location"));
        }

        btnSave = findViewById(R.id.btnSave);
        changePW = findViewById(R.id.textViewChangePwd);
    }

    private void initListeners() {
        imageViewProfile.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(1));
        btnSave.setOnClickListener(view12 -> {
            if (isCheckValidation()) sendData();
        });
        mLocationAutoTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                manageAutoComplete(s.toString(), "location");
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        changePW.setOnClickListener(view14 -> showDilogChangePassword());
    }

    private void manageAutoComplete(String query, String type) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest.Builder request = FindAutocompletePredictionsRequest.builder();
//        request.setCountry("US");
        if(type.equals("address"))
            request.setTypeFilter(TypeFilter.ADDRESS);
        else // location
            request.setTypeFilter(TypeFilter.REGIONS);

        request.setSessionToken(token)
                .setQuery(query);


        placesClient.findAutocompletePredictions(request.build()).addOnSuccessListener((response) -> {

            ids.clear();
            places.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                places.add(prediction.getFullText(null).toString());
                ids.add(prediction.getPlaceId());
                Log.i("Places", prediction.getPlaceId());
                Log.i("Places", prediction.getFullText(null).toString());
            }
            String[] data = places.toArray(new String[places.size()]); // terms is a List<String>

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, data);
            mLocationAutoTextView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
            }
        });

    }

    private boolean isCheckValidation() {
        boolean checkValidation = true;

        String getFirstName = editTextFirstName.getText().toString();
        String getLastName = editTextLastName.getText().toString();
        String getEmailId = editTextEmail.getText().toString();

        // Pattern match for email id
        Pattern p = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");
        Matcher m = p.matcher(getEmailId);
        // Check if all strings are null or not
        if (getFirstName.isEmpty()) {
            editTextFirstName.setError("!");
            checkValidation = false;
        }
        if (getLastName.isEmpty()) {
            editTextLastName.setError("!");
            checkValidation = false;
        }
        if (getEmailId.isEmpty()) {
            editTextEmail.setError("!");
            checkValidation = false;
        }

        // Check if all strings are null or not
        if (editTextAddress1.getText().toString().isEmpty()) {
            editTextAddress1.setError("!");
            checkValidation = false;
        }
        if (mLocationAutoTextView.getText().toString().isEmpty()) {
            mLocationAutoTextView.setError("!");
            checkValidation = false;
        }
        if (editTextPostalCode.getText().toString().isEmpty()) {
            editTextPostalCode.setError("!");
            checkValidation = false;
        }
        if (etPhoneNumber.getText().toString().isEmpty()) {
            etPhoneNumber.setError("!");
            checkValidation = false;
        }

        // Check if email id valid or not
        if (!m.find()) {
            editTextEmail.setError("!");
            checkValidation = false;
        }

        return checkValidation;
    }

    private void sendData() {

        if (SettingsMain.isConnectingToInternet(this)) {


            JsonObject params = new JsonObject();
            params.addProperty("first_name", editTextFirstName.getText().toString());
            params.addProperty("last_name", editTextLastName.getText().toString());
            params.addProperty("email", editTextEmail.getText().toString());
            params.addProperty("mobile", etPhoneNumber.getText().toString());
            params.addProperty("country_code", etCountryCode.getText().toString());
            params.addProperty("address1", editTextAddress1.getText().toString());
            params.addProperty("address2", editTextAddress2.getText().toString());
            params.addProperty("location", mLocationAutoTextView.getText().toString());
            params.addProperty("postalcode", editTextPostalCode.getText().toString());

            Log.d("info Send UpdatePofile", "" + params.toString());

                SettingsMain.showDilog(this);
                Call<ResponseBody> req = restService.postUpdateProfile(params, UrlController.AddHeaders(this));
                req.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseobj) {
                        try {
                            Log.d("info UpdateProfile Resp", "" + responseobj.toString());
                            if (responseobj.isSuccessful()) {

                                JSONObject response = new JSONObject(responseobj.body().string());
                                if (response.getBoolean("success")) {
                                    Log.d("info UpdateProfile obj", "" + response.toString());
                                    JSONObject data = response.getJSONObject("data");
                                    settingsMain.setUser(data.toString());
//                                    (new HomeActivity()).changeImage();

                                    Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                            Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                            Log.d("info UpdateProfile", "NullPointert Exception" + t.getLocalizedMessage());
                            SettingsMain.hideDilog();
                        } else {
                            SettingsMain.hideDilog();
                            Log.d("info UpdateProfile err", String.valueOf(t));
                            Log.d("info UpdateProfile err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        }
                    }
                });


        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {

        final CharSequence[] items;
        items = new CharSequence[]{"camera", "local" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Load Image");
        builder.setItems(items, (dialog, item) -> {
            if (item == 0) {
                {
                    userChoosenTask = "Take Photo";
                    cameraIntent();
                }
            } else if (item == 1) {
                {
                    userChoosenTask = "Choose from Library";
                    galleryIntent();
                }
            } else if (item == 3) {
                dialog.dismiss();
            }

        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
            if (requestCode == COUNTRYCODE_ACTION) {
                if (data != null) {
                    if (data.hasExtra("COUNTRY")) {
                        Country country = (Country) data.getSerializableExtra("COUNTRY");
                        this.mSelectedCountry = country;
                        setPhoneNumberHint();
                        etCountryCode.setText("+" + country.getPhoneCode() + "");
                        imgFlag.setImageResource(CountryUtils.getFlagDrawableResId(country.getIso()));
                    }
                }
            }
        }
    }

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Uri tempUri = SettingsMain.getImageUri(this, thumbnail);
        File finalFile = new File(SettingsMain.getRealPathFromURI(this, tempUri));
        galleryImageUpload(tempUri);
        switch (choosenImage) {
            case 1:
                imageViewProfile.setImageURI(tempUri);
                break;
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Uri tempUri = SettingsMain.getImageUri(this, bm);

                galleryImageUpload(tempUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        switch (choosenImage) {
            case 1:
                imageViewProfile.setImageBitmap(bm);
                break;
        }
    }

    private void galleryImageUpload(final Uri absolutePath) {

        if (SettingsMain.isConnectingToInternet(this)) {

            SettingsMain.showDilog(this);
            final File finalFile = new File(SettingsMain.getRealPathFromURI(this, absolutePath));
            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse(getBaseContext().getContentResolver().getType(absolutePath)),
                            finalFile
                    );
            MultipartBody.Part body = null;


            switch (choosenImage) {
                case 1:
                    body = MultipartBody.Part.createFormData("profile_img", finalFile.getName(), requestFile);
                    break;

            }

            Call<ResponseBody> req = restService.postUploadProfileImage(body, UrlController.UploadImageAddHeaders(this));
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.v("info Upload Responce", response.toString());
                        if (response.isSuccessful()) {
                            JSONObject responseobj = null;

                            responseobj = new JSONObject(response.body().string());
                            if (responseobj.getBoolean("success")) {
                                try {
                                    Toast.makeText(getBaseContext(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //noinspection ResultOfMethodCallIgnored
                                finalFile.delete();
                            } else {
                                Toast.makeText(getBaseContext(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }


                        }
                        else {
                            Log.e("update profile error", response.errorBody().string());
                            Toast.makeText(getBaseContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Upload profile", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info Upload profile err", String.valueOf(t));
                        Log.d("info Upload profile err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    public void hideSoftKeyboard() {
        if (this.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    void showDilogChangePassword() {

        final Dialog dialog = new Dialog(this, R.style.customDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);
        //noinspection ConstantConditions
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));

        Button Send = dialog.findViewById(R.id.send_button);
        Button Cancel = dialog.findViewById(R.id.cancel_button);

        final EditText editTextOld = dialog.findViewById(R.id.editText);
        final EditText editTextNew = dialog.findViewById(R.id.editText2);
        final EditText editTextConfirm = dialog.findViewById(R.id.editText3);

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(editTextOld.getText().toString()) &&
                        !TextUtils.isEmpty(editTextNew.getText().toString()) &&
                        !TextUtils.isEmpty(editTextConfirm.getText().toString()))
                    if (editTextNew.getText().toString().equals(editTextConfirm.getText().toString())) {

                        if (SettingsMain.isConnectingToInternet(ProfileEditActivity.this)) {

                            SettingsMain.showDilog(ProfileEditActivity.this);

                            JsonObject params = new JsonObject();
                            params.addProperty("old_password", editTextOld.getText().toString());
                            params.addProperty("password", editTextNew.getText().toString());
                            params.addProperty("password_confirmation", editTextConfirm.getText().toString());

                            Log.d("info sendChange Passwrd", params.toString());
                            Call<ResponseBody> myCall = restService.postChangePasswordEditProfile(params, UrlController.AddHeaders(ProfileEditActivity.this));
                            myCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                                    try {
                                        Log.d("info ChangePassword Res", "" + responseObj.toString());
                                        if (responseObj.isSuccessful()) {

                                            JSONObject response = new JSONObject(responseObj.body().string());
                                            if (response.getBoolean("success")) {
                                                dialog.dismiss();
                                                settingsMain.setUserPassword(editTextNew.getText().toString());
                                                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    if (t instanceof TimeoutException) {
                                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                                        SettingsMain.hideDilog();
                                    }
                                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                                        SettingsMain.hideDilog();
                                    }
                                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                                        Log.d("info ChangePassword ", "NullPointert Exception" + t.getLocalizedMessage());
                                        SettingsMain.hideDilog();
                                    } else {
                                        SettingsMain.hideDilog();
                                        Log.d("info ChangePassword err", String.valueOf(t));
                                        Log.d("info ChangePassword err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                                    }
                                }
                            });
                        } else {
                            SettingsMain.hideDilog();
                            Toast.makeText(getBaseContext(), "Internet error", Toast.LENGTH_SHORT).show();
                        }
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

    private void setPhoneUI() {
        mPhoneUtil = PhoneNumberUtil.createInstance(this);
        TelephonyManager tm = (TelephonyManager) this.getSystemService(this.getApplicationContext().TELEPHONY_SERVICE);
        String countryISO = tm.getNetworkCountryIso();
        String countryNumber = "";
        String countryName = "";
        Utility.log(countryISO);

        if(!TextUtils.isEmpty(countryISO))
        {
            for (Country country : CountryUtils.getAllCountries(this)) {
                if (countryISO.toLowerCase().equalsIgnoreCase(country.getIso().toLowerCase())) {
                    countryNumber = country.getPhoneCode();
                    countryName = country.getName();
                    break;
                }
            }
            Country country = new Country(countryISO,
                    countryNumber,
                    countryName);
            this.mSelectedCountry = country;
            etCountryCode.setText("+" + country.getPhoneCode() + "");
            imgFlag.setImageResource(CountryUtils.getFlagDrawableResId(country.getIso()));
            Utility.log(countryNumber);
        }
        else {
            Country country = new Country(getString(com.phonenumberui.R.string.country_united_states_code),
                    getString(com.phonenumberui.R.string.country_united_states_number),
                    getString(com.phonenumberui.R.string.country_united_states_name));
            this.mSelectedCountry = country;
            etCountryCode.setText("+" + country.getPhoneCode() + "");
            imgFlag.setImageResource(CountryUtils.getFlagDrawableResId(country.getIso()));
            Utility.log(countryNumber);
        }

        setPhoneNumberHint();
        etCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyBoardFromView(ProfileEditActivity.this);
                etPhoneNumber.setError(null);
                Intent intent = new Intent(ProfileEditActivity.this, CountryCodeActivity.class);
                intent.putExtra("TITLE", getResources().getString(com.phonenumberui.R.string.app_name));
                startActivityForResult(intent, COUNTRYCODE_ACTION);
            }
        });
    }
    private void setPhoneNumberHint() {
        if (mSelectedCountry != null) {
            Phonenumber.PhoneNumber phoneNumber =
                    mPhoneUtil.getExampleNumberForType(mSelectedCountry.getIso().toUpperCase(),
                            PhoneNumberUtil.PhoneNumberType.MOBILE);
            if (phoneNumber != null) {
                String format = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                if (format.length() > mSelectedCountry.getPhoneCode().length())
                    etPhoneNumber.setHint(
                            format.substring((mSelectedCountry.getPhoneCode().length() + 1), format.length()));
            }
        }
    }

    @Override
    public void onSuccessPermission(int code) {
        choosenImage = code;
        selectImage();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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