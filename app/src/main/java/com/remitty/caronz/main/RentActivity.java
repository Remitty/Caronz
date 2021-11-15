package com.remitty.caronz.main;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.remitty.caronz.R;
import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.avis.AvisPickupActivity;
import com.remitty.caronz.home.HomeActivity;

public class RentActivity extends AppCompatActivity {
    LinearLayout ownerLayout, avisLayout, budgetLayout, paylessLayout;

    public String method="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ownerLayout = findViewById(R.id.owner_layout);
        avisLayout = findViewById(R.id.avis_layout);
        paylessLayout = findViewById(R.id.payless_layout);
        budgetLayout = findViewById(R.id.budget_layout);


        ownerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RentActivity.this, HomeActivity.class);
                intent.putExtra("method", "rent");
                overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                startActivity(intent);
//                FragmentSearch fragment_cat = new FragmentSearch();
//                Bundle bundle = new Bundle();
//                bundle.putString("method", "rent");
//                bundle.putString("catId", "0");
//                fragment_cat.setArguments(bundle);
//                replaceFragment(fragment_cat, "FragmentSearch");
            }
        });

        avisLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RentActivity.this, AvisPickupActivity.class);
                intent.putExtra("brand", "Avis");
                startActivity(intent);
            }
        });

        paylessLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RentActivity.this, AvisPickupActivity.class);
                intent.putExtra("brand", "Payless");
                startActivity(intent);
            }
        });

        budgetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RentActivity.this, AvisPickupActivity.class);
                intent.putExtra("brand", "Budget");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }
}