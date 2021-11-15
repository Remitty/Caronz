package com.remitty.caronz.main;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.remitty.caronz.R;
import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.Search.HireSearchMapFragment;
import com.remitty.caronz.Settings.Settings;
import com.remitty.caronz.auth.AuthActivity;
import com.remitty.caronz.cars.MyCarsActivity;
import com.remitty.caronz.coins.CoinActivity;
import com.remitty.caronz.hire.MyHireActivity;
import com.remitty.caronz.home.AddNewAdPost;
import com.remitty.caronz.home.FragmentAllCategories;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.messages.ChatActivity;
import com.remitty.caronz.messages.Inbox;
import com.remitty.caronz.orders.MyBookingActivity;
import com.remitty.caronz.others.ActivityHelp;
import com.remitty.caronz.others.NotificationFragment;
import com.remitty.caronz.payment.CardsActivity;
import com.remitty.caronz.profile.FragmentProfile;
import com.remitty.caronz.utills.CircleTransform;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.withdraw.WithdrawActivity;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener{

    LinearLayout buyLayout, rentLayout, hireLayout, postLayout;
    TextView tvGreeting;
    TextView textViewUserName;
    ImageView imageViewProfile;

    private SettingsMain settingsMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        
        initComponents();

        initListeners();

        initNavigation();

        changeImage();
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

    private void initComponents() {
        settingsMain = new SettingsMain(this);

        buyLayout = findViewById(R.id.buy_layout);
        rentLayout = findViewById(R.id.rent_layout);
        hireLayout = findViewById(R.id.hire_layout);
        postLayout = findViewById(R.id.post_layout);
        tvGreeting = findViewById(R.id.tv_greeting);

        tvGreeting.setText("Good morning " + settingsMain.getUserFirstName());
    }

    private void initListeners() {
        buyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentAllCategories fragment_cat = new FragmentAllCategories();
//                Bundle bundle = new Bundle();
//                bundle.putString("method", "buy");
//                fragment_cat.setArguments(bundle);
//                replaceFragment(fragment_cat, "FragmentAllCategories");
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("method", "buy");
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                startActivity(intent);
            }
        });

        rentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AvisFragment fragment_cat = new AvisFragment();
//                replaceFragment(fragment_cat, "AvisFragment");
                startActivity(new Intent(MainActivity.this, RentActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            }
        });

        hireLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("method", "hire");
                startActivity(intent);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
//                HireSearchMapFragment fragment_cat = new HireSearchMapFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("catId", "0");
//                fragment_cat.setArguments(bundle);
//                replaceFragment(fragment_cat, "HireSearchMapFragment");
            }
        });

        postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddNewAdPost.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            }
        });
    }

    private void initNavigation() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();

        if (!settingsMain.getAppOpen()) {
//                                    menu.findItem(R.id.message).setVisible(false);
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_withdraw).setVisible(false);
            menu.findItem(R.id.nav_myOrders).setVisible(false);
            menu.findItem(R.id.nav_myHire).setVisible(false);
            menu.findItem(R.id.nav_myCars).setVisible(false);
            menu.findItem(R.id.nav_cards).setVisible(false);

            menu.findItem(R.id.nav_log_out).setVisible(false);
            menu.findItem(R.id.nav_log_in).setVisible(true);
        }

        View header = navigationView.getHeaderView(0);

        if (header != null) {
            TextView textViewUserEmail = header.findViewById(R.id.useremail);
            textViewUserName = header.findViewById(R.id.username);
            imageViewProfile = header.findViewById(R.id.imageView);
            TextView tvEditProfile = header.findViewById(R.id.tv_edit);


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
                tvEditProfile.setVisibility(View.GONE);
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

            tvEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
//                    replaceFragment(new FragmentProfile(), "FragmentProfile");
                }
            });
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        item.setChecked(true);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        switch(id){
            case R.id.nav_rent:
//                fragment = new FragmentSearch();
//                tag = "FragmentSearch";
//                bundle.putString("method", "rent");
//                fragment.setArguments(bundle);
                startActivity(new Intent(MainActivity.this, RentActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_sale:
//                fragment = new FragmentSearch();
//                tag = "FragmentBuySearch";
//                Bundle bundle1 = new Bundle();
//                bundle1.putString("method", "buy");
//                fragment.setArguments(bundle1);
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                intent.putExtra("method", "buy");
                startActivity(intent);
                break;
            case R.id.nav_hire:
//                fragment = new HireSearchMapFragment();
//                tag = "HireSearchMapFragment";
                Intent intent1 = new Intent(MainActivity.this, HomeActivity.class);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                intent1.putExtra("method", "hire");
                startActivity(intent1);
                break;
            case R.id.nav_profile:
//                fragment = new FragmentProfile();
//                tag = "FragmentProfile";
                break;
            case R.id.nav_message:
                Intent intent2 = new Intent(getApplicationContext(), ChatActivity.class);
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                intent2.putExtra("receiverId", "");
                startActivity(intent2);
                break;
            case R.id.nav_cards:
                startActivity(new Intent(getApplicationContext(), CardsActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_myOrders:
                startActivity(new Intent(getApplicationContext(), MyBookingActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_myHire:
                startActivity(new Intent(getApplicationContext(), MyHireActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_withdraw:
                startActivity(new Intent(getApplicationContext(), WithdrawActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_coins:
                startActivity(new Intent(getApplicationContext(), CoinActivity.class));
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
//            case R.id.nav_notification:
//                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
//                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
//                break;
            case R.id.nav_log_in:
                startActivity(new Intent(getApplicationContext(), AuthActivity.class));
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                break;
            case R.id.nav_log_out:

                alert.setTitle(getBaseContext().getResources().getString(R.string.app_name));
                alert.setCancelable(false);
                alert.setIcon(R.mipmap.ic_launcher);
                alert.setMessage("Do you want to logout?");
                alert.setPositiveButton(settingsMain.getAlertOkText(), (dialog, which) -> {
                    settingsMain.setFireBaseId("");
                    MainActivity.this.finish();
                    Intent intent4 = new Intent(MainActivity.this, AuthActivity.class);
                    intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent4);
                    overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                    settingsMain.setUser("");
                    settingsMain.setAppOpen(false);
                    dialog.dismiss();
                });
                alert.setNegativeButton(settingsMain.getAlertCancelText(), (dialogInterface, i) -> dialogInterface.dismiss());
                alert.show();
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}