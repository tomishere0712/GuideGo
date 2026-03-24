package com.example.guidego.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.guidego.R;
import com.example.guidego.ui.auth.LoginActivity;
import com.example.guidego.utils.TokenManager;

public class AdminProfileFragment extends Fragment {

    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvEmail = view.findViewById(R.id.tv_email);
        com.google.android.material.textfield.TextInputEditText etName = view.findViewById(R.id.et_name);
        com.google.android.material.textfield.TextInputEditText etEmail = view.findViewById(R.id.et_email);
        View btnLogout = view.findViewById(R.id.btn_logout);

        String name = tokenManager.getUserName();
        String email = tokenManager.getUserEmail();

        tvName.setText(name != null && !name.isEmpty() ? name : "Admin");
        tvEmail.setText(email != null ? email : "");
        etName.setText(name != null ? name : "");
        etEmail.setText(email != null ? email : "");

        btnLogout.setOnClickListener(v -> confirmLogout());
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

