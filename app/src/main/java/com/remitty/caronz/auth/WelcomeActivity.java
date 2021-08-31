package com.remitty.caronz.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.remitty.caronz.R;
import com.remitty.caronz.home.HomeActivity;

public class WelcomeActivity extends AppCompatActivity {
    private ImageView closeImage;
    private Button btnHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        setTitle(R.string.app_name);

        closeImage = findViewById(R.id.closeIcon);
        btnHome = findViewById(R.id.contineBuyPackage);



        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            }
        });
    }
}
