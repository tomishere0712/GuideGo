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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterGuideActivity extends AppCompatActivity {

    private ActivityRegisterGuideBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        int experience;
        try {
            experience = Integer.parseInt(expStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số năm kinh nghiệm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> languages = new ArrayList<>();
        if (!langsStr.isEmpty()) {
            for (String p : langsStr.split(",")) {
                String t = p.trim();
                if (!t.isEmpty()) languages.add(t);
            }
        }

        // Backend đọc user_id từ DTO body — phải gửi kèm
        String userId = new TokenManager(this).getUserId();
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
                            // Show actual server error
                            String errMsg = "Đăng ký thất bại (code " + response.code() + ")";
                            try {
                                if (response.errorBody() != null) {
                                    String body = response.errorBody().string();
                                    // Extract message field if present
                                    if (body.contains("\"message\"")) {
                                        int start = body.indexOf("\"message\"") + 11;
                                        int end = body.indexOf("\"", start);
                                        if (end > start) errMsg = body.substring(start, end);
                                    } else if (!body.isEmpty()) {
                                        errMsg = body.length() > 120 ? body.substring(0, 120) : body;
                                    }
                                }
                            } catch (IOException ignored) {}
                            Toast.makeText(RegisterGuideActivity.this, errMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateDataResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(RegisterGuideActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
