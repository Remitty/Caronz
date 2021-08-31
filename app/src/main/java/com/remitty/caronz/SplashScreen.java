package com.remitty.caronz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONObject;

import com.remitty.caronz.auth.ProfileCompleteActivity;
import com.remitty.caronz.helper.LocaleHelper;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.auth.MainActivity;
import com.remitty.caronz.utills.SettingsMain;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class SplashScreen extends AppCompatActivity {

    public static JSONObject jsonObjectAppRating;
    public static boolean gmap_has_countries = false;
    public static String gmap_countries;
    Activity activity;
    SettingsMain setting;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Configuration configuration = getResources().getConfiguration();
        configuration.fontScale = (float) 1; //0.85 small size, 1 normal size, 1,15 big etc

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);

        activity = this;
        setting = new SettingsMain(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        setting.setDeviceToken(token);
                        // Log and toast
                        Log.d("FIREbaseTAG", token);
//                        Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
                    }
                });

        if (SettingsMain.isConnectingToInternet(this)) {
//            setSettings();
            if (setting.getAppOpen()) {
                if(!setting.getProfileComplete()) {
                    startActivity(new Intent(activity, ProfileCompleteActivity.class));
                    activity.finish();
                    return;
                }
                Intent intent = new Intent(activity, HomeActivity.class);
                startActivity(intent);
                activity.overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                activity.finish();
            } else {
                SplashScreen.this.finish();
                setting.setUser("");
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(SplashScreen.this);
            alert.setTitle(setting.getAlertDialogTitle("error"));
            alert.setCancelable(false);
            alert.setMessage(setting.getAlertDialogMessage("internetMessage"));
            alert.setPositiveButton(setting.getAlertOkText(), (dialog, which) -> {
                dialog.dismiss();
                SplashScreen.this.recreate();
            });
            alert.show();
        }


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

}
