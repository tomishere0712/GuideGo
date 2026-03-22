package com.example.guidego;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guidego.ui.admin.AdminMainActivity;
import com.example.guidego.ui.auth.LoginActivity;
import com.example.guidego.ui.guide.GuideMainActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TokenManager tokenManager = new TokenManager(this);
            Intent intent;
            if (tokenManager.isLoggedIn()) {
                String role = tokenManager.getUserRole();
                if (Constants.ROLE_ADMIN.equals(role)) {
                    intent = new Intent(this, AdminMainActivity.class);
                } else if (Constants.ROLE_GUIDE.equals(role)) {
                    intent = new Intent(this, GuideMainActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
