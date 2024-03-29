package com.remitty.caronz.auth;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.remitty.caronz.R;
import com.remitty.caronz.helper.LocaleHelper;
import com.remitty.caronz.utills.SettingsMain;

public class AuthActivity extends AppCompatActivity {
    private static FragmentManager fragmentManager;
    SettingsMain settingsMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        settingsMain = new SettingsMain(this);
//        settingsMain.setMainColor("#D5E7D7");

        setContentView(R.layout.activity_auth);
        fragmentManager = getSupportFragmentManager();

        // If savedinstnacestate is null then replace login fragment
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("page", false)) {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.frameContainer, new SignUp_Fragment(),
                                Utils.Login_Fragment).commit();
            } else
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.frameContainer, new Login_Fragment(),
                                Utils.Login_Fragment).commit();
        }
        updateViews(settingsMain.getAlertDialogMessage("gmap_lang"));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void updateViews(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
    }

    // Replace Login Fragment with animation
    protected void replaceLoginFragment() {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.frameContainer, new Login_Fragment(),
                        Utils.Login_Fragment).commit();
    }

    @Override
    public void onBackPressed() {

        // Find the tag of signup and forgot password fragment
        Fragment SignUp_Fragment = fragmentManager
                .findFragmentByTag(Utils.SignUp_Fragment);
        Fragment ForgotPassword_Fragment = fragmentManager
                .findFragmentByTag(Utils.ForgotPassword_Fragment);
        Fragment VerifyAccount_Fragment = fragmentManager
                .findFragmentByTag(Utils.VerifyAccount_Fragment);



        // Check if both are null or not
        // If both are not null then replace login fragment else do backpressed
        // task

        if (SignUp_Fragment != null)
            replaceLoginFragment();
        else if (ForgotPassword_Fragment != null)
            replaceLoginFragment();
        else if (VerifyAccount_Fragment != null)
            replaceLoginFragment();

        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.left_enter, R.anim.right_out);
        }
    }
}
