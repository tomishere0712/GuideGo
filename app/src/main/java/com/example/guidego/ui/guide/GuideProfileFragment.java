package com.example.guidego.ui.guide;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.FragmentGuideProfileBinding;
import com.example.guidego.model.Guide;
import com.example.guidego.model.User;
import com.example.guidego.model.request.UpdateGuideRequest;
import com.example.guidego.model.request.UpdateProfileRequest;
import com.example.guidego.model.response.GuidesResponse;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.auth.LoginActivity;
import com.example.guidego.utils.TokenManager;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuideProfileFragment extends Fragment {

    private FragmentGuideProfileBinding binding;
    private TokenManager tokenManager;
    private Guide currentGuide;
    private User currentUser;
    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        binding.btnLogout.setOnClickListener(v -> confirmLogout());
        binding.btnEdit.setOnClickListener(v -> toggleEdit());
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.btnRegisterGuide.setOnClickListener(v -> openRegisterGuide());

        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String userId = tokenManager.getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        // Load user info
        ApiClient.getInstance(requireContext()).getApiService()
                .getUserById(userId)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            currentUser = response.body();
                            displayUserInfo();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
                });

        // Load guide profile
        ApiClient.getInstance(requireContext()).getApiService()
                .getGuideByUserId(userId)
                .enqueue(new Callback<GuidesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GuidesResponse> call,
                                           @NonNull Response<GuidesResponse> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && !response.body().getData().isEmpty()) {
                            currentGuide = response.body().getData().get(0);
                            displayGuideInfo();
                        } else {
                            // No guide profile
                            binding.cardGuideInfo.setVisibility(View.GONE);
                            binding.cardRegisterGuide.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GuidesResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void displayUserInfo() {
        if (currentUser == null) {
            binding.tvName.setText(tokenManager.getUserName());
            binding.tvEmail.setText(tokenManager.getUserEmail());
            return;
        }
        binding.tvName.setText(currentUser.getFullName());
        binding.tvEmail.setText(currentUser.getEmail());
        binding.etName.setText(currentUser.getFullName());
        binding.etPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(currentUser.getAvatarUrl()).into(binding.ivAvatar);
        }
    }

    private void displayGuideInfo() {
        if (currentGuide == null) return;
        binding.cardGuideInfo.setVisibility(View.VISIBLE);
        binding.cardRegisterGuide.setVisibility(View.GONE);

        if (currentGuide.isVerified()) {
            binding.layoutVerified.setVisibility(View.VISIBLE);
            binding.tvVerifiedLabel.setText("Đã xác minh");
        } else {
            binding.layoutVerified.setVisibility(View.VISIBLE);
            binding.tvVerifiedLabel.setText("Chờ duyệt");
        }

        binding.tvExperience.setText(currentGuide.getExperienceYears() + " năm kinh nghiệm");
        String langs = currentGuide.getLanguages() != null
                ? String.join(", ", currentGuide.getLanguages()) : "—";
        binding.tvLanguages.setText(langs);
        binding.tvRating.setText("⭐ " + currentGuide.getRating());
        binding.tvDescription.setText(currentGuide.getDescription() != null
                ? currentGuide.getDescription() : "");
    }

    private void toggleEdit() {
        isEditing = !isEditing;
        binding.etName.setEnabled(isEditing);
        binding.etPhone.setEnabled(isEditing);
        binding.btnSave.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        binding.btnEdit.setText(isEditing ? "Hủy" : "Chỉnh sửa");
    }

    private void saveProfile() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Tên không được trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = tokenManager.getUserId();
        UpdateProfileRequest request = new UpdateProfileRequest(name, phone);
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getInstance(requireContext()).getApiService()
                .updateUser(userId, request)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            toggleEdit();
                            loadProfile();
                        } else {
                            Toast.makeText(requireContext(), "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openRegisterGuide() {
        startActivity(new Intent(requireContext(), RegisterGuideActivity.class));
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    tokenManager.clearToken();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

