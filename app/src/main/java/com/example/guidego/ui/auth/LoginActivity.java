package com.example.guidego.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guidego.MainActivity;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityLoginBinding;
import com.example.guidego.model.request.LoginRequest;
import com.example.guidego.model.response.LoginResponse;
import com.example.guidego.ui.admin.AdminMainActivity;
import com.example.guidego.ui.guide.GuideMainActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) { binding.tilEmail.setError("Vui lòng nhập email"); return; }
        if (TextUtils.isEmpty(password)) { binding.tilPassword.setError("Vui lòng nhập mật khẩu"); return; }
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        setLoading(true);
        ApiClient.getInstance(this).getApiService()
                .login(new LoginRequest(email, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // saveToken decodes JWT and stores role, userId, email, name
                            tokenManager.saveToken(response.body().getToken());

                            // Route to correct main activity based on role
                            String role = tokenManager.getUserRole();
                            Intent intent;
                            if (Constants.ROLE_ADMIN.equals(role)) {
                                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                            } else if (Constants.ROLE_GUIDE.equals(role)) {
                                intent = new Intent(LoginActivity.this, GuideMainActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }
}



