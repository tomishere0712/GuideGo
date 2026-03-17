package com.example.guidego.ui.profile;

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
import com.example.guidego.databinding.FragmentProfileBinding;
import com.example.guidego.model.User;
import com.example.guidego.model.request.UpdateProfileRequest;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.auth.LoginActivity;
import com.example.guidego.utils.FormatUtils;
import com.example.guidego.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private TokenManager tokenManager;
    private boolean isEditing = false;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        binding.btnEdit.setOnClickListener(v -> toggleEditMode());
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());

        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String userId = tokenManager.getUserId();
        if (userId.isEmpty()) {
            displayFromToken();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getUserById(userId)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            currentUser = response.body();
                            displayUser(currentUser);
                        } else {
                            displayFromToken();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        displayFromToken();
                    }
                });
    }

    private void displayFromToken() {
        binding.tvName.setText(tokenManager.getUserName());
        binding.tvEmail.setText(tokenManager.getUserEmail());
        binding.tvRole.setText(getRoleLabel(tokenManager.getUserRole()));
        binding.etName.setText(tokenManager.getUserName());
        binding.etPhone.setText(tokenManager.getUserPhone()); // use locally cached phone
    }

    private void displayUser(User user) {
        binding.tvName.setText(user.getFullName());
        binding.tvEmail.setText(user.getEmail());
        binding.tvRole.setText(getRoleLabel(user.getRole()));
        binding.etName.setText(user.getFullName());
        String phone = user.getPhone() != null ? user.getPhone() : "";
        binding.etPhone.setText(phone);
        // Cache phone locally so it's available even without API access
        if (!phone.isEmpty()) tokenManager.saveUserPhone(phone);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .into(binding.ivAvatar);
        }
    }

    private String getRoleLabel(String role) {
        if (role == null) return "Du khách";
        switch (role) {
            case "Tourist": return "Du khách";
            case "Guide": return "Hướng dẫn viên";
            case "Admin": return "Quản trị viên";
            case "Company": return "Công ty du lịch";
            default: return role;
        }
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        binding.etName.setEnabled(isEditing);
        binding.etPhone.setEnabled(isEditing);
        binding.btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        binding.btnSave.setVisibility(isEditing ? View.VISIBLE : View.GONE);
    }

    private void saveProfile() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String userId = tokenManager.getUserId();
        String email = currentUser != null ? currentUser.getEmail() : tokenManager.getUserEmail();

        if (name.isEmpty()) { Toast.makeText(requireContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show(); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        UpdateProfileRequest req = new UpdateProfileRequest(name, email, phone);

        ApiClient.getInstance(requireContext()).getApiService()
                .updateUser(userId, req)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            // Cache updated phone/name locally
                            tokenManager.saveUserPhone(phone);
                            Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            toggleEditMode();
                            loadProfile();
                        } else {
                            Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    tokenManager.clearToken();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

