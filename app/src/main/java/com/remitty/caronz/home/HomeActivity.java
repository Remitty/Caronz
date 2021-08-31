package com.remitty.caronz.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.badge.BadgeDrawable;
import com.remitty.caronz.Notification.Config;
import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.messages.ChatActivity;
import com.remitty.caronz.messages.Inbox;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.gson.JsonObject;

import com.remitty.caronz.orders.MyBookingActivity;
import com.remitty.caronz.payment.CardsActivity;
import com.remitty.caronz.payment.WithdrawActivity;

import com.remitty.caronz.cars.MyCarsActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.Settings.Settings;
import com.remitty.caronz.SplashScreen;
import com.remitty.caronz.helper.LocaleHelper;
import com.remitty.caronz.profile.FragmentProfile;
import com.remitty.caronz.auth.MainActivity;
import com.remitty.caronz.utills.CircleTransform;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

public class
HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RuntimePermissionHelper.permissionInterface, BottomNavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener {

    public static Activity activity;
    public static Boolean checkLoading = false;
    boolean back_pressed = false;
    private MyReceiver receiver;
    MenuItem action_notification;

    SettingsMain settingsMain;
    ImageView imageViewProfile;
    UpdateFragment updatfrag;
    TextView textViewUserName;
    RestService restService;
    AutoCompleteTextView currentLocationText;
    RuntimePermissionHelper runtimePermissionHelper;
    private PlacesClient placesClient;
    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();

    public void updateApi(UpdateFragment listener) {
        updatfrag = listener;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        System.gc();
        settingsMain = new SettingsMain(this);
        runtimePermissionHelper = new RuntimePermissionHelper(this, this);
        activity = this;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer,
                    new HomeFragment()).commit();
        }

        restService = UrlController.createService(RestService.class);// for test
        
        receiveNotification();

    }

    private void receiveNotification() {
        IntentFilter filter = new IntentFilter(Config.PUSH_NOTIFICATION);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        if (!settingsMain.getNotificationTitle().equals("")) {
            String title, message, image;
            title = settingsMain.getNotificationTitle();
            message = settingsMain.getNotificationMessage();
            image = settingsMain.getNotificationImage();

            showNotificationDialog(title, message, image);
            updateViews(settingsMain.getAlertDialogMessage("gmap_lang"));
        }
    }

    private void initNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(this);
        
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();

        if (!settingsMain.getAppOpen()) {
//                                    menu.findItem(R.id.message).setVisible(false);
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_withdraw).setVisible(false);
            menu.findItem(R.id.nav_myOrders).setVisible(false);
            menu.findItem(R.id.nav_myCars).setVisible(false);
            menu.findItem(R.id.nav_cards).setVisible(false);

            menu.findItem(R.id.nav_log_out).setVisible(false);
            menu.findItem(R.id.nav_log_in).setVisible(true);
        }

        View header = navigationView.getHeaderView(0);

        if (header != null) {
            TextView textViewUserEmail = header.findViewById(R.id.textView);
            textViewUserName = header.findViewById(R.id.username);
            imageViewProfile = header.findViewById(R.id.imageView);


            int[] colors = {Color.parseColor(getMainColor()), Color.parseColor(getMainColor())};
            //create a new gradient color
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT, colors);
            gd.setCornerRadius(0f);

            header.setBackground(gd);


            if (!settingsMain.getAppOpen()) {
                if (!TextUtils.isEmpty(settingsMain.getGuestImage())) {
                    Picasso.with(this).load(settingsMain.getGuestImage())
                            .transform(new CircleTransform())
                            .error(R.drawable.placeholder)
                            .placeholder(R.drawable.placeholder)
                            .into(imageViewProfile);
                }
            } else {
                if (!TextUtils.isEmpty(settingsMain.getUser())) {
                    textViewUserEmail.setText(settingsMain.getUserEmail());
                }
                if (!TextUtils.isEmpty(settingsMain.getUser())) {
                    textViewUserName.setText(settingsMain.getUserName());
                }
                if (!TextUtils.isEmpty(settingsMain.getUserImage())) {
                    Picasso.with(this).load(settingsMain.getUserImage())
                            .transform(new CircleTransform())
                            .error(R.drawable.placeholder)
                            .placeholder(R.drawable.placeholder)
                            .into(imageViewProfile);
                }
            }
        }
    }


//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(LocaleHelper.onAttach(base));
//    }

    private void updateViews(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
    }

    public void showRatingDialog() {
        String title = null, text = null, btn_confirm = null, btn_cancel = null, url = null;
        try {
            title = SplashScreen.jsonObjectAppRating.getString("title");
            btn_confirm = SplashScreen.jsonObjectAppRating.getString("btn_confirm");
            btn_cancel = SplashScreen.jsonObjectAppRating.getString("btn_cancel");
            url = SplashScreen.jsonObjectAppRating.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("this", "Feedback:");


        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .session(3)
                .threshold(3)
                .title(title)
                .positiveButtonText(btn_confirm)
                .negativeButtonText(btn_cancel)
                .ratingBarColor(R.color.yellow)
                .playstoreUrl(url)
                .onRatingBarFormSumbit(feedback -> Log.i("this", "Feedback:" + feedback))
                .build();


        ratingDialog.show();
    }

    private void showNotificationDialog(String title, String message, String image) {

        final Dialog dialog;
        dialog = new Dialog(HomeActivity.this, R.style.customDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_notification_layout);
        ImageView imageView = dialog.findViewById(R.id.notificationImage);
        TextView tv_title = dialog.findViewById(R.id.notificationTitle);
        TextView tV_message = dialog.findViewById(R.id.notificationMessage);
        Button button = dialog.findViewById(R.id.cancel_button);
        button.setText(settingsMain.getGenericAlertCancelText());
        button.setBackgroundColor(Color.parseColor(getMainColor()));


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));


        if (!TextUtils.isEmpty(image)) {
            Picasso.with(this).load(image)
                    .fit()
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }

        tv_title.setText(title);
        tV_message.setText(message);

        button.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void changeImage() {
        if (!TextUtils.isEmpty(settingsMain.getUser())){

            if (!TextUtils.isEmpty(settingsMain.getUserImage())) {
                Picasso.with(this).load(settingsMain.getUserImage())
                        .transform(new CircleTransform())
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(imageViewProfile);
            }
            textViewUserName.setText(settingsMain.getUserName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
//        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        action_notification = menu.findItem(R.id.action_notification);
        MenuItem action_location = menu.findItem(R.id.action_location);
        action_location.setOnMenuItemClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }


    public void replaceFragment(Fragment someFragment, String tag) {

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        Fragment fragment2 = fm.findFragmentById(R.id.frameContainer);

        if (fragment != fragment2) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
            transaction.replace(R.id.frameContainer, someFragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    private void AddFirebaseid() {
        if (SettingsMain.isConnectingToInternet(this)) {


            JsonObject params = new JsonObject();


            params.addProperty("firebase_id", "");

            Call<ResponseBody> myCall = restService.postFirebaseId(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info FireBase Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info Data FireBase", response.getJSONObject("data").toString());
                                settingsMain.setFireBaseId(response.getJSONObject("data").getString("firebase_reg_id"));
                                Log.d("info FireBase ID", response.getJSONObject("data").getString("firebase_reg_id"));
                                Log.d("info FireBase", "Firebase id is set with server.!");
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
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info FireBase ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info FireBase err", String.valueOf(t));
                        Log.d("info FireBase err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void locationSearch() {

//        gps = new GPSTracker(HomeActivity.this);

//        List<Address> addresses1 = null;
//        if (gps.canGetLocation()) {
//            latitude = gps.getLatitude();
//            longitude = gps.getLongitude();

            final Dialog dialog = new Dialog(HomeActivity.this, R.style.customDialog);

            dialog.setCanceledOnTouchOutside(true);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_location_seekbar);


            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));
            Button Send = dialog.findViewById(R.id.send_button);
            Button Cancel = dialog.findViewById(R.id.cancel_button);

            currentLocationText = dialog.findViewById(R.id.et_location);

            placesClient = com.google.android.libraries.places.api.Places.createClient(this);
            currentLocationText.setOnItemClickListener(this);
            currentLocationText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    manageAutoComplete(s.toString());

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            Send.setOnClickListener(v -> {
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentByTag("FragmentSearch");
                Fragment fragment2 = fm.findFragmentById(R.id.frameContainer);

                FragmentSearch fragment_search = new FragmentSearch();
                Bundle bundle = new Bundle();
                bundle.putString("location", currentLocationText.getText().toString());

                fragment_search.setArguments(bundle);

                if (fragment != fragment2) {
                    replaceFragment(fragment_search, "FragmentSearch");
                } else {
//                    updatfrag.updatefrag(nearby_latitude, nearby_longitude, nearby_distance, currentLocationText.getText().toString());
                }
                dialog.dismiss();
            });
            Cancel.setOnClickListener(v -> {
//                adforest_changeNearByStatus("", ""
//                        , Integer.toString(bubbleSeekBar.getProgress()));
                dialog.dismiss();
            });

            dialog.show();
//        } else
//            gps.showSettingsAlert();
    }

    private void manageAutoComplete(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest.Builder request = FindAutocompletePredictionsRequest.builder();

//        request.setCountry("US");
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

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(HomeActivity.this, android.R.layout.simple_dropdown_item_1line, data);
            currentLocationText.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
            }
        });


    }



    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            backPressed();
//            super.onBackPressed();
            overridePendingTransition(R.anim.left_enter, R.anim.right_out);
        }
    }

    private void backPressed() {
        if (!back_pressed) {
            Toast.makeText(HomeActivity.this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
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

            AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
            alert.setTitle(settingsMain.getAlertDialogTitle("info"));
            alert.setCancelable(false);
            alert.setMessage("Are you sure you want to exit?");
            alert.setPositiveButton(settingsMain.getAlertOkText(), (dialog, which) -> {
                HomeActivity.this.finishAffinity();
                dialog.dismiss();
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            });
            alert.setNegativeButton(settingsMain.getAlertCancelText(), (dialogInterface, i) -> dialogInterface.dismiss());
            alert.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.d("info onDestroy called", "onDestroy");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(true);

        Fragment fragment = null;
        Bundle bundle = new Bundle();
        String tag = null;
        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
        switch(id){
//            case R.id.nav_home:
//                FragmentManager fm = HomeActivity.this.getSupportFragmentManager();
//                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
//                    fm.popBackStack();
//                }
//                break;
            case R.id.nav_rent:
                fragment = new FragmentAllCategories();
                tag = "FragmentAllCategories";
                bundle.putString("method", "rent");
                fragment.setArguments(bundle);
                break;
            case R.id.nav_sale:
                FragmentAllCategories fm1 = new FragmentAllCategories();
                Bundle bundle1 = new Bundle();
                bundle1.putString("method", "buy");
                fm1.setArguments(bundle1);
                replaceFragment(fm1, "FragmentAllCategories");
                break;
            case R.id.nav_profile:
                replaceFragment(new FragmentProfile(), "FragmentProfile");
                break;
            case R.id.nav_message:
                Intent intent1 = new Intent(getApplicationContext(), ChatActivity.class);
                intent1.putExtra("receiverId", "");
                startActivity(intent1);
                break;
            case R.id.nav_cards:
                startActivity(new Intent(getApplicationContext(), CardsActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_myOrders:
                startActivity(new Intent(getApplicationContext(), MyBookingActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_withdraw:
                startActivity(new Intent(getApplicationContext(), WithdrawActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_myCars:
                startActivity(new Intent(getApplicationContext(), MyCarsActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(), Settings.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_support:
                startActivity(new Intent(getApplicationContext(), ActivityHelp.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_log_in:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_log_out:

                alert.setTitle(getBaseContext().getResources().getString(R.string.app_name));
                alert.setCancelable(false);
                alert.setIcon(R.mipmap.ic_launcher);
                alert.setMessage("Do you want to logout?");
                alert.setPositiveButton(settingsMain.getAlertOkText(), (dialog, which) -> {
                    settingsMain.setFireBaseId("");
                    HomeActivity.this.finish();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                    settingsMain.setUser("");
                    settingsMain.setAppOpen(false);
                    dialog.dismiss();
                });
                alert.setNegativeButton(settingsMain.getAlertCancelText(), (dialogInterface, i) -> dialogInterface.dismiss());
                alert.show();
                break;
            case R.id.nav_find:
                fragment = new HomeFragment();
                tag="HomeFragment";
                break;
            case R.id.nav_chat:
                fragment = new Inbox();
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                BadgeDrawable badge =  bottomNav.getOrCreateBadge(R.id.nav_chat);
                badge.setVisible(false);
                break;
            case R.id.nav_post:
                if (settingsMain.getAppOpen()) {
                    runtimePermissionHelper.requestStorageCameraPermission(1);
                } else {
                    //Alert dialog for exit form Home screen
                    alert.setTitle(settingsMain.getAlertDialogTitle("info"));
                    alert.setCancelable(false);
                    alert.setMessage("You can't post now. please sign in app.");
                    alert.setPositiveButton(settingsMain.getAlertOkText(), (dialog, which) -> {
                        dialog.dismiss();
                    });
                    alert.setNegativeButton(settingsMain.getAlertCancelText(), (dialogInterface, i) -> dialogInterface.dismiss());
                    alert.show();
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (fragment != null) {
            replaceFragment(fragment, tag);
        }

        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onSuccessPermission(int code) {
        // check phone verification
        if (code == 1) {
            if (settingsMain.getAppOpen()) {
                Intent intent = new Intent(HomeActivity.this, AddNewAdPost.class);
                startActivity(intent);
            } else {
                //Alert dialog for exit form Home screen
                AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                alert.setTitle(settingsMain.getAlertDialogTitle("info"));
                alert.setCancelable(false);
                alert.setMessage("You can't post now. please sign in app.");
                alert.setPositiveButton(settingsMain.getAlertOkText(), (dialog, which) -> {
                    dialog.dismiss();
                });
                alert.setNegativeButton(settingsMain.getAlertCancelText(), (dialogInterface, i) -> dialogInterface.dismiss());
                alert.show();
            }
        } else { // code = 2
            locationSearch();
        }
    }



    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
//                runtimePermissionHelper.requestLocationPermission(2);
                locationSearch();
                break;
            case R.id.action_notification:
                item.setVisible(false);
                Intent intent1 = new Intent(HomeActivity.this, ChatActivity.class);
                intent1.putExtra("receiverId", "");
                startActivity(intent1);
                break;
        }
        return false;
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

    public interface UpdateFragment {
        void updatefrag(String s);

        void updatefrag(String latitude, String longitude, String distance);
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String kind = intent.getStringExtra("kind");
            Log.e("received action", kind);
            if(kind.equals(Config.PUSH_NOTIFICATION))
                action_notification.setVisible(true);
            else {
                    BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                    BadgeDrawable badge =  bottomNav.getOrCreateBadge(R.id.nav_chat);
                    badge.setVisible(true);
            }

        }
    }
}