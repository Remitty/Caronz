package com.remitty.caronz.auth;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
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

public class ForgotPassword_Fragment extends Fragment implements
        OnClickListener {
    Activity activity;
    SettingsMain settingsMain;
    LinearLayout linearLayoutLogo;
    RestService restService;
    private View view;
    private EditText emailId;
    private TextView submit, back, headingText;
    private ImageView imageViewLogo;

    public ForgotPassword_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.forgotpassword_layout, container,
                false);

        activity = getActivity();
        settingsMain = new SettingsMain(activity);
        restService = UrlController.createService(RestService.class);
        initViews();
        setListeners();
        return view;
    }

    // Initialize the views
    @SuppressWarnings("ResourceType")
    private void initViews() {

        emailId = view.findViewById(R.id.registered_emailid);
        submit = view.findViewById(R.id.forgot_button);
        back = view.findViewById(R.id.backToLoginBtn);

        imageViewLogo = view.findViewById(R.id.logoimage);
        headingText = view.findViewById(R.id.heading);
        linearLayoutLogo = view.findViewById(R.id.logo);

        linearLayoutLogo.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));


        // Setting text selector over textviews
        XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            //noinspection deprecation
            ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

            back.setTextColor(csl);
            submit.setTextColor(csl);

        } catch (Exception e) {
            Log.d("err", e.toString());
        }

    }

    // Set Listeners over buttons
    private void setListeners() {
        back.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backToLoginBtn:

                // Replace Login Fragment on Back Presses
                new AuthActivity().replaceLoginFragment();
                break;

            case R.id.forgot_button:

                // Call Submit button task
                submitButtonTask();
                break;

        }

    }

    private void submitButtonTask() {
        String getEmailId = emailId.getText().toString();

        // Pattern for email id validation
        Pattern p = Pattern.compile(Utils.regEx);

        // Match the pattern
        Matcher m = p.matcher(getEmailId);

        // First check if email id is not null else show error toast
        if (getEmailId.equals("") || getEmailId.length() == 0)
            emailId.setError("");
            // Check if email id is valid or not
        else if (!m.find())
            emailId.setError("");
            // Else submit email id and fetch passwod or do your stuff
        else
            chngePswrd(getEmailId);
    }

    private void chngePswrd(String emilId) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("email", emilId);

            RestService restService =
                    UrlController.createService(RestService.class);
            Call<ResponseBody> myCall = restService.postForgotPassword(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info LoginPost responce", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {
                            Log.d("info LoginPost", "" + responseObj.body().toString());

                            JSONObject response = new JSONObject(responseObj.body().string());

                            Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_LONG).show();
                            SettingsMain.hideDilog();
                            emailId.setText("");
                            JSONObject user = response.optJSONObject("user");
                            String otp = user.optString("otp");
                            String id = user.optString("id");
                            getFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                                    .replace(R.id.frameContainer,
                                            new ResetPasswordFragment(otp, id),
                                            Utils.ForgotPassword_Fragment).commit();

                        } else {
                            settingsMain.showAlertDialog(getActivity(), responseObj.errorBody().string());
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
    public void onResume() {
        super.onResume();
    }
}