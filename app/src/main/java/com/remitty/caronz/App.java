package com.remitty.caronz;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.google.android.libraries.places.api.Places;

import com.remitty.caronz.R;
import com.remitty.caronz.helper.LocaleHelper;
import com.remitty.caronz.utills.NoInternet.AppLifeCycleManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        MobileAds.initialize(this, String.valueOf(R.string.Admob_app_id));
        AppLifeCycleManager.init(this);
        String apiKey = getString(R.string.places_api_key);
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
        MultiDex.install(this);
    }
}
