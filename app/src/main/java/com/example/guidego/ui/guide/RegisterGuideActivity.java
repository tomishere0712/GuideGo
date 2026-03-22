package com.example.guidego.ui.guide;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityRegisterGuideBinding;
import com.example.guidego.model.request.RegisterGuideRequest;
import com.example.guidego.model.response.CreateDataResponse;
import com.example.guidego.utils.TokenManager;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterGuideActivity extends AppCompatActivity {

    private ActivityRegisterGuideBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submitRegistration());
    }

    private void submitRegistration() {
        String expStr = binding.etExperience.getText() != null
                ? binding.etExperience.getText().toString().trim() : "";
        String langsStr = binding.etLanguages.getText() != null
                ? binding.etLanguages.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null
                ? binding.etDescription.getText().toString().trim() : "";

        if (expStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số năm kinh nghiệm", Toast.LENGTH_SHORT).show();
            return;
        }

        int experience = Integer.parseInt(expStr);
        List<String> languages = null;
        if (!langsStr.isEmpty()) {
            String[] parts = langsStr.split(",");
            languages = new java.util.ArrayList<>();
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) languages.add(trimmed);
            }
        }

        String userId = tokenManager.getUserId();
        RegisterGuideRequest request = new RegisterGuideRequest(userId, experience, languages, description);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        ApiClient.getInstance(this).getApiService()
                .registerGuide(request)
                .enqueue(new Callback<CreateDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateDataResponse> call,
                                           @NonNull Response<CreateDataResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterGuideActivity.this,
                                    "Đăng ký thành công! Vui lòng chờ Admin duyệt.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterGuideActivity.this,
                                    "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateDataResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(RegisterGuideActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

