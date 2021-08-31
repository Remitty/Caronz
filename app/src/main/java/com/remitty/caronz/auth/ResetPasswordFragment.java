package com.remitty.caronz.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.google.gson.JsonObject;

import java.io.IOException;

public class ResetPasswordFragment extends Fragment {

    EditText editOTP, editNewPW, editConfirmNewPW;
    Button btnSend;
    String otp, userID;

    public ResetPasswordFragment(String otp, String id) {
        // Required empty public constructor
        this.otp = otp;
        this.userID = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        editOTP = view.findViewById(R.id.edit_otp);
        editNewPW = view.findViewById(R.id.edit_new_password);
        editConfirmNewPW = view.findViewById(R.id.edit_new_password_confirm);
        btnSend = view.findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                    sendNewPassword();
            }
        });

        return view;
    }

    private boolean validate() {
        boolean validate = true;

        if(editOTP.getText().toString().equals("")){
            validate = false;
            editOTP.setError("!");
        }

        if(!editOTP.getText().toString().equals(this.otp)){
            validate = false;
            editOTP.setError("Not match!");
        }

        if(editNewPW.getText().toString().equals("")){
            validate = false;
            editNewPW.setError("!");
        }

        if(editNewPW.getText().toString().length() < 6){
            validate = false;
            editNewPW.setError("At least 6 characters");
        }

        if(editConfirmNewPW.getText().toString().equals("")){
            validate = false;
            editConfirmNewPW.setError("!");
        }

        if(!editConfirmNewPW.getText().toString().equals(editNewPW.getText().toString())){
            validate = false;
            editConfirmNewPW.setError("Not match password");
        }


        return validate;
    }

    private void sendNewPassword() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("password", editNewPW.getText().toString());
            params.addProperty("password_confirmation", editConfirmNewPW.getText().toString());
            params.addProperty("id", userID);
            Log.d("info Reset", "" + params.toString());
            RestService restService =
                    UrlController.createService(RestService.class);
            Call<ResponseBody> myCall = restService.postResetPassword(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Reset", "" + responseObj.toString());
                            Log.d("info Reset", "" + responseObj.body().string());

//                            JSONObject response = new JSONObject(responseObj.body().string());

                            Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_LONG).show();
                            SettingsMain.hideDilog();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        }
                        SettingsMain.hideDilog();
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
}
