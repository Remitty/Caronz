package com.remitty.caronz.profile;


import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.models.DocumentModel;
import com.remitty.caronz.models.UserModel;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;


public class FragmentProfile extends Fragment {

    SettingsMain settingsMain;
    RestService restService;

    TextView editProfBtn;

    TextView textViewUserName, textViewEmailvalue, textViewPhonevalue, textViewLocationvalue, tvAddress, tvAddress2, tvBalance, tvZipcode;
    ImageView imageViewProfile;
    ImageView licenseImage, registrationImage, insuranceImage, otherImage1, otherImage2;
    Dialog dialog;

    @Override
    public void onStop() {
        super.onStop();
        if (settingsMain != null) {
            settingsMain.setUserVerified(true);
        }
    }

    public FragmentProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        textViewUserName = view.findViewById(R.id.text_viewName);

        editProfBtn = view.findViewById(R.id.editProfile);

        imageViewProfile = view.findViewById(R.id.image_view);
//        ratingBar = view.findViewById(R.id.ratingBar);

//        LayerDrawable stars = (LayerDrawable) this.ratingBar.getProgressDrawable();
//        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

        textViewEmailvalue = view.findViewById(R.id.textViewEmailValue);
        textViewPhonevalue = view.findViewById(R.id.textViewPhoneValue);
        textViewLocationvalue = view.findViewById(R.id.tv_location);
        tvAddress = view.findViewById(R.id.tv_address);
        tvAddress2 = view.findViewById(R.id.tv_address2);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvZipcode = view.findViewById(R.id.tv_zipcode);

        textViewUserName.setText(settingsMain.getUserName());
        textViewEmailvalue.setText(settingsMain.getUserEmail());

        licenseImage = view.findViewById(R.id.image_license);
        registrationImage = view.findViewById(R.id.image_register);
        insuranceImage = view.findViewById(R.id.image_insurance);
        otherImage1 = view.findViewById(R.id.image_other1);
        otherImage2 = view.findViewById(R.id.image_other2);


        dialog = new Dialog(getActivity(), R.style.customDialog);

//        ((HomeActivity) getActivity()).changeImage();

        editProfBtn.setOnClickListener(view1 -> replaceFragment(new EditProfile(), "EditProfile"));

        setAllViewsText();

        return view;
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

                                textViewPhonevalue.setText(profile.getPhone());
                                textViewLocationvalue.setText(profile.getLocation());
                                tvAddress.setText(profile.getFirstAddress());
                                tvAddress2.setText(profile.getSecondAddress());
                                tvZipcode.setText(profile.getPostalCode());
                                tvBalance.setText("$ " + profile.getBalance());

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

}
