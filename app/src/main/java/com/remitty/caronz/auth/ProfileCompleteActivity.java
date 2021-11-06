package com.remitty.caronz.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.remitty.caronz.R;
import com.remitty.caronz.home.AddNewAdPost;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.google.gson.JsonObject;
import com.phonenumberui.CountryCodeActivity;
import com.phonenumberui.countrycode.Country;
import com.phonenumberui.countrycode.CountryUtils;
import com.phonenumberui.utility.Utility;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static android.view.View.INVISIBLE;

public class ProfileCompleteActivity extends AppCompatActivity implements RuntimePermissionHelper.permissionInterface{
    private SettingsMain settingsMain;
    RuntimePermissionHelper runtimePermissionHelper;
    RestService restService;

    FrameLayout page1, page2;
    FrameLayout frameLayout;
    TextView textViewUserName, textViewUserEmail;
    TextView textViewName, textViewLocation, textViewMainTitle, textViewImage, textViewIntroduction;
    ImageView photoImage, licenseImage, registrationImage, insuranceImage, otherImage1, otherImage2;
    EditText  editTextAddress1, editTextAddress2, editTextCity, editTextState, editTextCountry, editTextPostalCode;
    CircleImageView imageViewProfile;
    TextView btnSkip;
    Button btnNext, btnSave;
    Spinner spinnerACCType;
    private AppCompatEditText etCountryCode;
    private AppCompatEditText etPhoneNumber;
    private ImageView imgFlag;
    private Country mSelectedCountry;
    private static final int COUNTRYCODE_ACTION = 10001;
    private PhoneNumberUtil mPhoneUtil;

    AutoCompleteTextView mLocationAutoTextView;
    private PlacesClient placesClient;

    boolean checkValidation = true;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();
    private String firstName, lastName, email;
    private int choosenImage;
    private String userChoosenTask;

    MultipartBody.Part profileImg, license, registration, insurance, other1, other2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_complete);


        settingsMain = new SettingsMain(this);

        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);
        runtimePermissionHelper = new RuntimePermissionHelper(this, this);
        placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(settingsMain.getMainColor())));
        getSupportActionBar().setTitle("Complete Profile");

        initComponents();
        initListeners();

        firstName = settingsMain.getUserName();
        email = settingsMain.getUserEmail();

        textViewUserEmail.setText(email);
        textViewUserName.setText(firstName);

        setPhoneUI();

    }

    private void setPhoneUI() {
        mPhoneUtil = PhoneNumberUtil.createInstance(ProfileCompleteActivity.this);
        TelephonyManager tm = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        String countryISO = tm.getNetworkCountryIso();
        String countryNumber = "";
        String countryName = "";
        Utility.log(countryISO);

        if(!TextUtils.isEmpty(countryISO))
        {
            for (Country country : CountryUtils.getAllCountries(ProfileCompleteActivity.this)) {
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
                Utility.hideKeyBoardFromView(ProfileCompleteActivity.this);
                etPhoneNumber.setError(null);
                Intent intent = new Intent(ProfileCompleteActivity.this, CountryCodeActivity.class);
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

    private void initListeners() {
        photoImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(1));
        licenseImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(2));
        registrationImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(3));
        insuranceImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(4));
        otherImage1.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(5));
//        otherImage2.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(6));

        btnSave.setOnClickListener(view12 -> {
            if (license != null || registration != null || insurance != null || other1 != null) galleryImageUpload();
            else Toast.makeText(getBaseContext(), "No image to upload.", Toast.LENGTH_SHORT).show();
        });

        btnNext.setOnClickListener(view15 -> {
            if (isCheckValidation()) sendData();
        });

        btnSkip.setOnClickListener(view14 -> {
            startActivity(new Intent(ProfileCompleteActivity.this, WelcomeActivity.class));
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

    }

    private void initComponents() {
        page1 = findViewById(R.id.content_profile_complete);
        page2 = findViewById(R.id.content_document_upload);
        page2.setVisibility(View.GONE);
        textViewName = findViewById(R.id.textViewName);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewMainTitle = findViewById(R.id.textView);
        textViewIntroduction = findViewById(R.id.textViewIntroduction);
        textViewImage = findViewById(R.id.textViewSetImage);
        photoImage = findViewById(R.id.image_photo);
        licenseImage = findViewById(R.id.image_license);
        registrationImage = findViewById(R.id.image_register);
        insuranceImage = findViewById(R.id.image_insurance);
        otherImage1 = findViewById(R.id.image_other1);
//        otherImage2 = findViewById(R.id.image_other2);
        textViewUserName = findViewById(R.id.text_viewName);
        textViewUserEmail = findViewById(R.id.text_viewEmail);
        spinnerACCType = findViewById(R.id.spinner);

        imageViewProfile = findViewById(R.id.image_view);

        editTextPostalCode = findViewById(R.id.editTextPostalCode);
        editTextAddress1 = findViewById(R.id.editTextAddress1);
        editTextAddress2 = findViewById(R.id.editTextAddress2);
        editTextCity = findViewById(R.id.editTextCity);
        editTextCountry = findViewById(R.id.editTextCountry);
        editTextState = findViewById(R.id.editTextState);
        mLocationAutoTextView = findViewById(R.id.editTextLocation);


        frameLayout = findViewById(R.id.frameLayout);
        btnSave = findViewById(R.id.btnSave);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.tv_skip);

        etCountryCode = findViewById(com.phonenumberui.R.id.etCountryCode);
        etPhoneNumber = findViewById(com.phonenumberui.R.id.etPhoneNumber);
        imgFlag = findViewById(com.phonenumberui.R.id.flag_imv);

    }

    private void manageAutoComplete(String query, String type) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest.Builder request = FindAutocompletePredictionsRequest.builder();
//        request.setCountry("US");
        if(type.equals("address"))
            request.setTypeFilter(TypeFilter.ADDRESS);
        else  // location
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

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(ProfileCompleteActivity.this, android.R.layout.simple_dropdown_item_1line, data);
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
        checkValidation = true;
        // Check if all strings are null or not
        if (editTextAddress1.getText().toString().isEmpty()) {
            editTextAddress1.setError("!");
            checkValidation = false;
        }
        if (textViewLocation.getText().toString().isEmpty()) {
            textViewLocation.setError("!");
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

        return checkValidation;
    }

    
    private void sendData() {


        if (SettingsMain.isConnectingToInternet(this)) {

            JsonObject params = new JsonObject();

            params.addProperty("email", settingsMain.getUserEmail());
            params.addProperty("first_name", settingsMain.getUserFirstName());
            params.addProperty("last_name", settingsMain.getUserSecondName());
            params.addProperty("mobile", etPhoneNumber.getText().toString());
            params.addProperty("country_code", etCountryCode.getText().toString());
            params.addProperty("address1", editTextAddress1.getText().toString());
            params.addProperty("address2", editTextAddress2.getText().toString());
            params.addProperty("location", mLocationAutoTextView.getText().toString());
            params.addProperty("postalcode", editTextPostalCode.getText().toString());

            Log.d("info Send UpdatePofile", "" + params.toString());
            if (!checkValidation) {
                Toast.makeText(getBaseContext(), "Invalid URL specified", Toast.LENGTH_SHORT).show();
            } else {
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

                                    Toast.makeText(getBaseContext(), "Completed profile successfully", Toast.LENGTH_SHORT).show();
                                    settingsMain.setUser(response.getString("data"));
                                    settingsMain.setProfileComplete(true);
                                    page1.setVisibility(View.GONE);
                                    page2.setVisibility(View.VISIBLE);
                                    frameLayout.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.right_enter));
                                    JSONObject data = response.getJSONObject("data");
                                    settingsMain.setUser(data.toString());
//                                    startActivity(new Intent(ProfileCompleteActivity.this, WelcomeActivity.class));
                                } else {
                                    Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("update profile error", responseobj.errorBody().string());
                                Toast.makeText(getBaseContext(), responseobj.errorBody().string(), Toast.LENGTH_SHORT).show();
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
            }

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {

        final CharSequence[] items;
        items = new CharSequence[]{"Camera", "Local" };

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

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Uri tempUri = SettingsMain.getImageUri(this, thumbnail);
        File finalFile = new File(SettingsMain.getRealPathFromURI(this, tempUri));
        galleryPrepare(tempUri);
        switch (choosenImage) {
            case 1:
                photoImage.setImageURI(tempUri);
                break;
            case 2:
                licenseImage.setImageURI(tempUri);
                break;
            case 3:
                registrationImage.setImageURI(tempUri);
                break;
            case 4:
                insuranceImage.setImageURI(tempUri);
                break;
            case 5:
                otherImage1.setImageURI(tempUri);
                break;
            case 6:
                otherImage2.setImageURI(tempUri);
                break;
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Uri tempUri = SettingsMain.getImageUri(this, bm);

                galleryPrepare(tempUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        switch (choosenImage) {
            case 1:
                photoImage.setImageBitmap(bm);
                break;
            case 2:
                licenseImage.setImageBitmap(bm);
                break;
            case 3:
                registrationImage.setImageBitmap(bm);
                break;
            case 4:
                insuranceImage.setImageBitmap(bm);
                break;
            case 5:
                otherImage1.setImageBitmap(bm);
                break;
            case 6:
                otherImage2.setImageBitmap(bm);
                break;
        }
    }

    private void galleryPrepare(final Uri absolutePath) {
        final File finalFile = new File(SettingsMain.getRealPathFromURI(this, absolutePath));
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getBaseContext().getContentResolver().getType(absolutePath)),
                        finalFile
                );

        switch (choosenImage) {
            case 1:
                profileImg = MultipartBody.Part.createFormData("profile_img", finalFile.getName(), requestFile);
                galleryProfileImageUpload();
                break;
            case 2:
                license = MultipartBody.Part.createFormData("license_img", finalFile.getName(), requestFile);
                break;
            case 3:
                registration = MultipartBody.Part.createFormData("registration_img", finalFile.getName(), requestFile);
                break;
            case 4:
                insurance = MultipartBody.Part.createFormData("insurance_img", finalFile.getName(), requestFile);
                break;
            case 5:
                other1 = MultipartBody.Part.createFormData("other1_img", finalFile.getName(), requestFile);
                break;
            case 6:
                other2 = MultipartBody.Part.createFormData("other2_img", finalFile.getName(), requestFile);
                break;
        }
    }

    private void galleryImageUpload() {

        if (SettingsMain.isConnectingToInternet(this)) {

            SettingsMain.showDilog(this);


            Call<ResponseBody> req = restService.postUploadProfileDocument(license, registration, insurance, other1, UrlController.UploadImageAddHeaders(this));
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    Log.v("info Upload Responce", response.toString());
                    try {
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
//                                finalFile.delete();
                                startActivity(new Intent(ProfileCompleteActivity.this, WelcomeActivity.class));
                            } else {
                                Toast.makeText(getBaseContext(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }


                    } else {
                            Log.e("update img error", response.errorBody().string());
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

    private void galleryProfileImageUpload() {

        if (SettingsMain.isConnectingToInternet(this)) {

            SettingsMain.showDilog(this);

            Call<ResponseBody> req = restService.postUploadProfileImage(profileImg, UrlController.UploadImageAddHeaders(this));
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    Log.v("info Upload Responce", response.toString());
                    try {
                        if (response.isSuccessful()) {
                            JSONObject responseobj = null;

                            responseobj = new JSONObject(response.body().string());

                            try {
                                Toast.makeText(getBaseContext(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.e("update img error", response.errorBody().string());
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

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSuccessPermission(int code) {
        choosenImage = code;
        selectImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            if (requestCode == REQUEST_CAMERA)
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
