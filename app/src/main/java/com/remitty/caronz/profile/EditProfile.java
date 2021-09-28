package com.remitty.caronz.profile;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.remitty.caronz.home.AddNewAdPost;
import com.remitty.caronz.models.DocumentModel;
import com.remitty.caronz.models.UserModel;
import com.google.gson.JsonObject;
import com.phonenumberui.CountryCodeActivity;
import com.phonenumberui.countrycode.Country;
import com.phonenumberui.countrycode.CountryUtils;
import com.phonenumberui.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.auth.MainActivity;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.squareup.picasso.Picasso;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.INVISIBLE;

public class EditProfile extends Fragment implements RuntimePermissionHelper.permissionInterface, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, AdapterView.OnItemClickListener{

    SettingsMain settingsMain;
    
    TextView btnCancel, btnSend, btnChangePwd, btnDeleteAccount;
    ImageView photoImage, licenseImage, registrationImage, insuranceImage, otherImage1, otherImage2;
    EditText editTextName, editTextLastName, editTextEmail, editTextAddress1, editTextAddress2, editTextCity, editTextPostalCode, editTextState, editTextCountry;
    CircleImageView imageViewProfile;
    Spinner spinnerACCType;
    LinearLayout viewSocialIconsLayout;
    RestService restService;
    boolean checkValidation = true;
    RuntimePermissionHelper runtimePermissionHelper;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    
    private boolean verify = false;
    private String userChoosenTask;

    private AppCompatEditText etCountryCode;
    private AppCompatEditText etPhoneNumber;
    private ImageView imgFlag;
    private Country mSelectedCountry;
    private static final int COUNTRYCODE_ACTION = 10001;
    private PhoneNumberUtil mPhoneUtil;

    AutoCompleteTextView mLocationAutoTextView;
    private PlacesClient placesClient;

    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();

    private View mView;
    private int choosenImage;

    public EditProfile() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());
//        publicProfileCustomIcons = view.findViewById(R.id.publicProfileCustomIcons);
        runtimePermissionHelper = new RuntimePermissionHelper(getActivity(), this);
        placesClient = com.google.android.libraries.places.api.Places.createClient(getActivity());

        initComponents();
        initListeners();

        setPhoneUI();

        setAllViewsText();
       
        return mView;
    }

    private void setPhoneUI() {
        mPhoneUtil = PhoneNumberUtil.createInstance(getActivity());
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(getActivity().getApplicationContext().TELEPHONY_SERVICE);
        String countryISO = tm.getNetworkCountryIso();
        String countryNumber = "";
        String countryName = "";
        Utility.log(countryISO);

        if(!TextUtils.isEmpty(countryISO))
        {
            for (Country country : CountryUtils.getAllCountries(getActivity())) {
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
                Utility.hideKeyBoardFromView(getActivity());
                etPhoneNumber.setError(null);
                Intent intent = new Intent(getActivity(), CountryCodeActivity.class);
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
        otherImage2.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(6));

        btnSend.setOnClickListener(view12 -> {
            if (isCheckValidation()) sendData();
        });
//        btnCancel.setOnClickListener(view13 -> this.onBackPressed());
        btnChangePwd.setOnClickListener(view14 -> showDilogChangePassword());
        btnDeleteAccount.setOnClickListener(v -> showDeteleDialog());

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

        btnSend = mView.findViewById(R.id.textViewSend);
        btnCancel = mView.findViewById(R.id.textViewCancel);
        btnChangePwd = mView.findViewById(R.id.textViewChangePwd);
        btnDeleteAccount =  mView.findViewById(R.id.deleteAccount);
        spinnerACCType =  mView.findViewById(R.id.spinner);

        imageViewProfile =  mView.findViewById(R.id.image_view);

        photoImage =  mView.findViewById(R.id.imageSelected);
        licenseImage = mView.findViewById(R.id.image_license);
        registrationImage = mView.findViewById(R.id.image_register);
        insuranceImage = mView.findViewById(R.id.image_insurance);
        otherImage1 = mView.findViewById(R.id.image_other1);
        otherImage2 = mView.findViewById(R.id.image_other2);
        
        
        viewSocialIconsLayout =  mView.findViewById(R.id.editProfileCustomLayout);
        viewSocialIconsLayout.setVisibility(INVISIBLE);

        editTextName = mView.findViewById(R.id.editTextName);
        editTextLastName = mView.findViewById(R.id.editTextLastName);
        editTextEmail = mView.findViewById(R.id.editTextEmail);

        editTextPostalCode =  mView.findViewById(R.id.editTextPostalCode);
        editTextAddress1 =  mView.findViewById(R.id.editTextAddress1);
        editTextAddress2 =  mView.findViewById(R.id.editTextAddress2);
        editTextCity =  mView.findViewById(R.id.editTextCity);
        editTextCountry =  mView.findViewById(R.id.editTextCountry);
        editTextState =  mView.findViewById(R.id.editTextState);

        etCountryCode =  mView.findViewById(com.phonenumberui.R.id.etCountryCode);
        etPhoneNumber =  mView.findViewById(com.phonenumberui.R.id.etPhoneNumber);
        imgFlag =  mView.findViewById(com.phonenumberui.R.id.flag_imv);

        mLocationAutoTextView = mView.findViewById(R.id.editTextLocation);

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

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_dropdown_item_1line, data);
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

        String getFirstName = editTextName.getText().toString();
        String getLastName = editTextLastName.getText().toString();
        String getEmailId = editTextEmail.getText().toString();

        // Pattern match for email id
        Pattern p = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");
        Matcher m = p.matcher(getEmailId);
        // Check if all strings are null or not
        if (getFirstName.isEmpty()) {
            editTextName.setError("!");
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
//        if (editTextState.getText().toString().isEmpty()) {
//            editTextState.setError("!");
//            checkValidation = false;
//        }
//        if (editTextCountry.getText().toString().isEmpty()) {
//            editTextCountry.setError("!");
//            checkValidation = false;
//        }
//        if (editTextCity.getText().toString().isEmpty()) {
//            editTextCity.setError("!");
//            checkValidation = false;
//        }
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

    private void setAllViewsText() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            Call<ResponseBody> myCall = restService.getEditProfileDetails(UrlController.AddHeaders(getActivity()));
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

                                UserModel profile = new UserModel(jsonObject);

                                getActivity().setTitle("Edit Profile");

                                Picasso.with(getContext()).load(settingsMain.getUserImage())
                                        .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(imageViewProfile);

                                Picasso.with(getContext()).load(settingsMain.getUserImage())
                                        .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(photoImage);

                                editTextName.setText(profile.getFirstName());
                                editTextLastName.setText(profile.getLastName());
                                editTextEmail.setText(profile.getEmail());
                                editTextPostalCode.setText(profile.getPostalCode());
                                editTextAddress1.setText(profile.getFirstAddress());
                                editTextAddress2.setText(profile.getSecondAddress());
                                editTextCountry.setText(profile.getCountry());
                                editTextState.setText(profile.getState());
                                mLocationAutoTextView.setText(profile.getLocation());
                                editTextCity.setText(profile.getCity());
                                etCountryCode.setText(profile.getCountryCode());
                                etPhoneNumber.setText(profile.getMobile());

                                DocumentModel document = profile.getDocument();
                                if(document != null) {
                                    Picasso.with(getActivity()).load(document.getLicense()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(licenseImage);
                                    Picasso.with(getActivity()).load(document.getRegistration()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(registrationImage);
                                    Picasso.with(getActivity()).load(document.getInsurance()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(insuranceImage);
                                    Picasso.with(getActivity()).load(document.getOther1()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(otherImage1);
                                    Picasso.with(getActivity()).load(document.getOther2()).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(otherImage2);

                                }

                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                    Log.d("info Edit Profile error", String.valueOf(t));
                    Log.d("info Edit Profile error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendData() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {


            JsonObject params = new JsonObject();
            params.addProperty("first_name", editTextName.getText().toString());
            params.addProperty("last_name", editTextLastName.getText().toString());
            params.addProperty("email", editTextEmail.getText().toString());
            params.addProperty("mobile", etPhoneNumber.getText().toString());
            params.addProperty("country_code", etCountryCode.getText().toString());
            params.addProperty("address1", editTextAddress1.getText().toString());
            params.addProperty("address2", editTextAddress2.getText().toString());
            params.addProperty("location", mLocationAutoTextView.getText().toString());
//            params.addProperty("city", editTextCity.getText().toString());
//            params.addProperty("country", editTextCountry.getText().toString());
//            params.addProperty("state", editTextState.getText().toString());
            params.addProperty("postalcode", editTextPostalCode.getText().toString());

            Log.d("info Send UpdatePofile", "" + params.toString());
            if (!checkValidation) {
                Toast.makeText(getContext(), "Invalid URL specified", Toast.LENGTH_SHORT).show();
            } else {
                SettingsMain.showDilog(getActivity());
                Call<ResponseBody> req = restService.postUpdateProfile(params, UrlController.UploadImageAddHeaders(getActivity()));
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
                                    ((HomeActivity) getActivity()).changeImage();

                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
//                                    if(!verify)
//                                        SettingsMain.reload(getActivity(), "EditProfile");
//                                    else{
                                        startActivity(new Intent(getActivity(), HomeActivity.class));
//                                    }
                                } else {
                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {

        final CharSequence[] items;
        items = new CharSequence[]{"camera", "local" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        Uri tempUri = SettingsMain.getImageUri(getActivity(), thumbnail);
        File finalFile = new File(SettingsMain.getRealPathFromURI(getActivity(), tempUri));
        galleryImageUpload(tempUri);
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
                bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                Uri tempUri = SettingsMain.getImageUri(getActivity(), bm);

                galleryImageUpload(tempUri);

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

    private void galleryImageUpload(final Uri absolutePath) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());
            final File finalFile = new File(SettingsMain.getRealPathFromURI(getActivity(), absolutePath));
            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse(getContext().getContentResolver().getType(absolutePath)),
                            finalFile
                    );
            MultipartBody.Part body = null;


            switch (choosenImage) {
                case 1:
                    body = MultipartBody.Part.createFormData("profile_img", finalFile.getName(), requestFile);
                    break;
                case 2:
                    body = MultipartBody.Part.createFormData("license_img", finalFile.getName(), requestFile);
                    break;
                case 3:
                    body = MultipartBody.Part.createFormData("registration_img", finalFile.getName(), requestFile);
                    break;
                case 4:
                    body = MultipartBody.Part.createFormData("insurance_img", finalFile.getName(), requestFile);
                    break;
                case 5:
                    body = MultipartBody.Part.createFormData("other1_img", finalFile.getName(), requestFile);
                    break;
                case 6:
                    body = MultipartBody.Part.createFormData("other2_img", finalFile.getName(), requestFile);
                    break;
            }

            Call<ResponseBody> req = restService.postUploadProfileImage(body, UrlController.UploadImageAddHeaders(getActivity()));
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
                                    Toast.makeText(getActivity(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //noinspection ResultOfMethodCallIgnored
                                finalFile.delete();
                            } else {
                                Toast.makeText(getActivity(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }


                    }
                    else {
                        Log.e("update profile error", response.errorBody().string());
                        Toast.makeText(getContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    


    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
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

    void showDilogChangePassword() {

        final Dialog dialog = new Dialog(getActivity(), R.style.customDialog);
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

                        if (SettingsMain.isConnectingToInternet(getActivity())) {

                            SettingsMain.showDilog(getActivity());

                            JsonObject params = new JsonObject();
                            params.addProperty("old_password", editTextOld.getText().toString());
                            params.addProperty("password", editTextNew.getText().toString());
                            params.addProperty("password_confirmation", editTextConfirm.getText().toString());

                            Log.d("info sendChange Passwrd", params.toString());
                            Call<ResponseBody> myCall = restService.postChangePasswordEditProfile(params, UrlController.AddHeaders(getActivity()));
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
                                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                                        SettingsMain.hideDilog();
                                    }
                                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
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

    void showDeteleDialog() {

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Delete Account");
        alert.setCancelable(false);
        alert.setMessage("Would you delete this account?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int which) {
                deleteAccount();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();


    }

    private void deleteAccount() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("user_id", settingsMain.getUserId());
            Log.d("info Send terms id =", "" + params.toString());

            Call<ResponseBody> myCall = restService.postDeleteAccount(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info terms responce ", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
                                settingsMain.setUser("");
                                settingsMain.setFireBaseId("");
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("com.adforest", MODE_PRIVATE).edit();
                                editor.putString("isSocial", "false");
                                editor.apply();
                                getActivity().finish();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                                if (settingsMain.getCheckOpen()) {
                                    settingsMain.setAppOpen(true);
                                }

                            } else {
                                Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info CustomPages ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info CustomPages err", String.valueOf(t));
                        Log.d("info CustomPages err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
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
}