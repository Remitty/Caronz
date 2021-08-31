package com.remitty.caronz.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.phonenumberui.PhoneNumberActivity;

public class SignUp_Fragment extends Fragment implements OnClickListener{

    private static FragmentManager fragmentManager;
    Activity activity;
    RestService restService;
    boolean is_verify_on = false;
    private View view;
    private EditText firstName, lastName, emailId, mobileNumber,
            password, passwordConfirm;
    private TextView login;
    private Button signUpButton;
    private CheckBox terms_conditions;
    private SettingsMain settingsMain;
    public static int APP_REQUEST_CODE = 99;

    ActionCodeSettings actionCodeSettings;

    public SignUp_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.signup_layout, container, false);
        activity = getActivity();
        settingsMain = new SettingsMain(activity);
        restService = UrlController.createService(RestService.class);

        initViews();

        setListeners();

        actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        .setUrl("https://joiintapp.com/verifysignup")
                        .setDynamicLinkDomain("joiint.page.link")
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.remitty.caronz",
                                true,
                                "12" )
                        .build();
        return view;
    }

    // Initialize all views
    @SuppressWarnings("ResourceType")
    private void initViews() {
        fragmentManager = getActivity().getSupportFragmentManager();

        firstName = view.findViewById(R.id.firstName);
        lastName = view.findViewById(R.id.lastName);
        emailId = view.findViewById(R.id.userEmailId);
        mobileNumber = view.findViewById(R.id.mobileNumber);
        password = view.findViewById(R.id.password);
        passwordConfirm = view.findViewById(R.id.password_confirm);
        signUpButton = view.findViewById(R.id.signUpBtn);
        login = view.findViewById(R.id.already_user);

        terms_conditions = view.findViewById(R.id.terms_conditions);


        // Setting text selector over textviews
        XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            @SuppressWarnings("deprecation") ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

//            login.setTextColor(csl);
            terms_conditions.setTextColor(csl);
        } catch (Exception ignored) {
        }


    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpBtn:

                // Call checkValidation method
//                phoneLogin();
                checkValidation();
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;

            case R.id.already_user:

                // Replace login fragment
                new MainActivity().replaceLoginFragment();
                break;
        }

    }

    // Check Validation Method
    private void checkValidation() {

        // Get all edittext texts
        String getFirstName = firstName.getText().toString();
        String getLastName = lastName.getText().toString();
        String getEmailId = emailId.getText().toString();
        String getMobileNumber = mobileNumber.getText().toString();
        String getPassword = password.getText().toString();
        String getPasswordConfirm = passwordConfirm.getText().toString();

        // Pattern match for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);
        boolean validFlag = true;
        // Check if all strings are null or not
        if (getFirstName.equals("") || getFirstName.length() == 0) {
            firstName.setError("!");
            validFlag = false;
        }
        if (getLastName.equals("") || getLastName.length() == 0) {
            lastName.setError("!");
            validFlag = false;
        }
        if (getEmailId.equals("") || getEmailId.length() == 0) {
            emailId.setError("!");
            validFlag = false;
        }
//        if (getMobileNumber.equals("") || getMobileNumber.length() == 0) {
//            mobileNumber.setError("!");
//            validFlag = false;
//        }
        if (getPassword.equals("") || getPassword.length() == 0) {
            password.setError("!");
            validFlag = false;
        }

        if(getPassword.length() < 6){
            password.setError("Above 6 characters");
            validFlag = false;
        }

        if(!getPasswordConfirm.equals(getPassword)) {
            passwordConfirm.setError("Doesn't match password.");
            validFlag = false;
        }
            // Check if email id valid or not
        if (!m.find()) {
            emailId.setError("!");
            validFlag = false;
        }

        if(validFlag) {
            settingsMain.showDilog(getActivity());
//            FirebaseAuth auth = FirebaseAuth.getInstance();
//            auth.sendSignInLinkToEmail(getEmailId, actionCodeSettings)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(getContext(), "Verified email successfully", Toast.LENGTH_SHORT).show();
////                                signinWithFirebaseEmail();
////                                setDynamicLinkForEmailVerify();
//                                signUp(getFirstName, getLastName, getEmailId, getMobileNumber, getPassword, getPasswordConfirm);
//                            } else {
//                                Toast.makeText(getContext(), "Verified email faild", Toast.LENGTH_SHORT).show();
//                                settingsMain.hideDilog();
//                            }
//                        }
//                    });
            signUp(getFirstName, getLastName, getEmailId, getMobileNumber, getPassword, getPasswordConfirm);
        }
            // Make sure user should check Terms and Conditions checkbox
//        else if (!terms_conditions.isChecked())
//            terms_conditions.setError("!");
//        else {
//            SharedPreferences.Editor editor = getActivity().getSharedPreferences("com.adforest", MODE_PRIVATE).edit();
//            editor.putString("isSocial", "false");
//            editor.apply();
//            signUp(getFullName, getEmailId, getMobileNumber, getPassword);
//        }
    }


    void signUp(String firstName, final String lastName, final String email, final String phone, final String pswd, String getPasswordConfirm) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            JsonObject params = new JsonObject();
            params.addProperty("first_name", firstName);
            params.addProperty("last_name", lastName);
            params.addProperty("email", email);
//            params.addProperty("phone", phone);
            params.addProperty("password", pswd);
            params.addProperty("password_confirmation", getPasswordConfirm);
            params.addProperty("device_token", settingsMain.getDeviceToken());

            Log.d("info Register param", "" + params.toString());
            Call<ResponseBody> myCall = restService.postRegister(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info SignUp Responce", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                if (is_verify_on) {
                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_LONG).show();
                                    Utils.user_id = response.getJSONObject("data").getString("id");
                                    Log.d("info SignUp Data", "" + response.getJSONObject("data"));
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            fragmentManager
                                                    .beginTransaction()
                                                    .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                                                    .replace(R.id.frameContainer,
                                                            new VerifyAccount_Fragment(),
                                                            Utils.VerifyAccount_Fragment).commit();
                                        }
                                    }, 1000);

                                } else {
                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                    new MainActivity().replaceLoginFragment();

                                }
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), responseObj.errorBody().toString(), Toast.LENGTH_SHORT).show();
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
                    Log.d("info SignUp error", String.valueOf(t));
                    Log.d("info SignUp error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            if (data != null && data.hasExtra("PHONE_NUMBER") && data.getStringExtra("PHONE_NUMBER") != null) {
                String phoneNumber = data.getStringExtra("PHONE_NUMBER");
//                settingsMain.setUserPhone(phoneNumber);
                activity.overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                activity.finish();
//                    mobileNumber = phoneNumber;
//                SharedHelper.putKey(RegisterActivity.this, "mobile", phoneNumber);
//                registerAPI();
            } else {
                // If mobile number is not verified successfully You can hendle according to your requirement.
                Toast.makeText(getActivity(),"Verification Faild",Toast.LENGTH_SHORT).show();
//                    Intent goToLogin = new Intent(RegisterActivity.this, WelcomeScreenActivity.class);
//                    goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(goToLogin);
//                    finish();
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void phoneLogin() {
        Intent intent = new Intent(getActivity(), PhoneNumberActivity.class);
        //Optionally you can add toolbar title
        intent.putExtra("TITLE", getResources().getString(R.string.app_name));
        //Optionally you can pass phone number to populate automatically.
        intent.putExtra("PHONE_NUMBER", "");
        startActivityForResult(intent, APP_REQUEST_CODE);
    }


}
