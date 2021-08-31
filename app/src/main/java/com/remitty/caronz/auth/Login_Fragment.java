package com.remitty.caronz.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.SplashScreen;
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
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import static com.remitty.caronz.R.anim;
import static com.remitty.caronz.R.id;

public class Login_Fragment extends Fragment implements OnClickListener {

    public static final int RC_SIGN_IN = 0;
    protected static String user_email;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;
    View view;
    Activity activity;
    EditText emailid, password;
    Button loginButton;
    TextView forgotPassword, signUp, startExplore;
    LinearLayout loginLayout;
    SettingsMain settingsMain;
    LinearLayout linearLayoutLogo;
    ImageView imageViewLogo;
    TextView textViewWelcome, textViewOR;
    LinearLayout guestLayout;
    RelativeLayout leftSideAttributLayout;
    boolean is_verify_on = false;
    private boolean mIntentInProgress = true;
    boolean back_pressed = false;

    public Login_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.login_layout, container, false);
        activity = getActivity();
        settingsMain = new SettingsMain(activity);
        initViews();
        setListeners();

        return view;
    }

    // Initiate Views
    private void initViews() {
        fragmentManager = getActivity().getSupportFragmentManager();

        emailid = view.findViewById(id.login_emailid);
        password = view.findViewById(id.login_password);
        loginButton = view.findViewById(id.loginBtn);
        forgotPassword = view.findViewById(id.forgot_password);
        signUp = view.findViewById(id.createAccount);
        loginLayout = view.findViewById(id.login_layout);
        startExplore = view.findViewById(id.startExplore);

        imageViewLogo = view.findViewById(id.logoimage);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                anim.shake);

    }

    // Set Listeners
    private void setListeners() {
        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);
        startExplore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case id.fbLogin:


                break;
            case id.loginBtn:
                checkValidation();
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;

            case id.forgot_password:

                // Replace forgot password fragment with animation
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(anim.right_enter, anim.left_out)
                        .replace(id.frameContainer,
                                new ForgotPassword_Fragment(),
                                Utils.ForgotPassword_Fragment).commit();
                break;
            case id.createAccount:

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(anim.right_enter, anim.left_out)
                        .replace(id.frameContainer, new SignUp_Fragment(),
                                Utils.SignUp_Fragment).commit();
                break;
            case id.startExplore:
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                activity.overridePendingTransition(anim.right_enter, anim.left_out);
                activity.finish();
                settingsMain.setUser("");
                break;
        }
    }


    // Check Validation before login
    private void checkValidation() {
        // Get email id and password
        String getEmailId = emailid.getText().toString();
        String getPassword = password.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);

        Matcher m = p.matcher(getEmailId);

        // Check for both field is empty or not
        if (getEmailId.equals("") && getEmailId.length() == 0) {
            if (getPassword.equals("") && getPassword.length() == 0) {
                loginLayout.startAnimation(shakeAnimation);
                emailid.setError("!");
                password.setError("!");
            }
        } else if (getEmailId.equals("") && getEmailId.length() == 0) {
            loginLayout.startAnimation(shakeAnimation);
            emailid.requestFocus();
            emailid.setError("!");
        } else if (getPassword.equals("") && getPassword.length() == 0) {
            loginLayout.startAnimation(shakeAnimation);
            password.setError("!");
            password.requestFocus();
        }
        // Check if email id is valid or not
        else if (!m.find()) {
            emailid.requestFocus();
            emailid.setError("!");
        }
        // Else do login and do your stuff
        else {
            login(getEmailId, getPassword);/// for test
        }

    }

    private void login(final String email, final String pswd) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("email", email);
            params.addProperty("password", pswd);
            params.addProperty("device_type", "android");
            params.addProperty("device_id", "android");
            params.addProperty("device_token", settingsMain.getDeviceToken());
            Log.d("enteries are", params.toString());

            RestService restService =
                    UrlController.createService(RestService.class);
            Call<ResponseBody> myCall = restService.postLogin(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info LoginPost responce", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = null;
                            try {
                                response = new JSONObject(responseObj.body().string());
                                if (response.getBoolean("success")) {
                                    if (is_verify_on && !response.getJSONObject("data").getBoolean("is_account_confirm")) {
                                        Log.d("info Login Resp", "" + response.getJSONObject("data"));
                                        Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_LONG).show();
                                        Utils.user_id = response.getJSONObject("data").getString("id");
                                        Handler handler = new Handler();
                                        handler.postDelayed(() -> fragmentManager
                                                .beginTransaction()
                                                .setCustomAnimations(anim.right_enter, anim.left_out)
                                                .replace(id.frameContainer,
                                                        new VerifyAccount_Fragment(),
                                                        Utils.VerifyAccount_Fragment).commit(), 1000);

                                    } else {
                                        Log.d("info Login Post", "" + response.toString());

                                        settingsMain.setUser(response.getString("user"));
                                        settingsMain.setAuthToken(response.getString("access_token"));
                                        settingsMain.setProfileComplete(response.getBoolean("isCompleteProfile"));
                                        settingsMain.setUserPassword(pswd);
                                        settingsMain.setAppOpen(true);
                                        Intent intent = new Intent(getActivity(), SplashScreen.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("login error", e.getMessage());
                            }


                        } else {
                            Toast.makeText(getActivity(), responseObj.errorBody().string(), Toast.LENGTH_SHORT).show();
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException | IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Log.d("info LoginPost error", String.valueOf(t));
                    Log.d("info LoginPost error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();
        this.getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

//This is the filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        backPressed();
                        break;
                }
                return true;
            }
        });
    }

    private void backPressed() {
        if (!back_pressed) {
            Toast.makeText(getContext(), "Press Again To Exit", Toast.LENGTH_SHORT).show();
            back_pressed = true;
            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    back_pressed = false;
                }
            }, 2000L);
        } else {

            //Alert dialog for exit form login screen

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(settingsMain.getAlertDialogTitle("info"));
            alert.setCancelable(false);
            alert.setMessage("Are you sure you want to exit?");
            alert.setPositiveButton(settingsMain.getAlertOkText(), (dialog, which) -> {
                getActivity().finishAffinity();
                dialog.dismiss();
            });
            alert.setNegativeButton(settingsMain.getAlertCancelText(), (dialogInterface, i) -> dialogInterface.dismiss());
            alert.show();
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

}
