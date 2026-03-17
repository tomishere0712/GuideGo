package com.example.guidego.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityRegisterBinding;
import com.example.guidego.model.request.RegisterRequest;
import com.example.guidego.model.response.StatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLoginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = getText(binding.etFullName);
        String email = getText(binding.etEmail);
        String phone = getText(binding.etPhone);
        String password = getText(binding.etPassword);

        if (TextUtils.isEmpty(name)) { binding.tilFullName.setError("Vui lòng nhập họ tên"); return; }
        if (TextUtils.isEmpty(email)) { binding.tilEmail.setError("Vui lòng nhập email"); return; }
        if (TextUtils.isEmpty(phone) || phone.length() != 10) { binding.tilPhone.setError("Số điện thoại phải đúng 10 chữ số"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { binding.tilPassword.setError("Mật khẩu tối thiểu 6 ký tự"); return; }

        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);

        setLoading(true);
        ApiClient.getInstance(this).getApiService()
                .register(new RegisterRequest(name, email, password, phone))
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String msg = "Đăng ký thất bại";
                            if (response.code() == 400) msg = "Email đã tồn tại hoặc dữ liệu không hợp lệ";
                            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StatusResponse> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }
}

