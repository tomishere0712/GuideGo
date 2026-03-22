package com.example.guidego.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.DialogAdminUserBinding;
import com.example.guidego.databinding.FragmentAdminUsersBinding;
import com.example.guidego.model.User;
import com.example.guidego.model.request.CreateUserRequest;
import com.example.guidego.model.request.UpdateProfileRequest;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersFragment extends Fragment {

    private FragmentAdminUsersBinding binding;
    private AdminUserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminUserAdapter(
                user -> showUserDialog(user),
                user -> confirmDeleteUser(user)
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadUsers);
        binding.fabCreate.setOnClickListener(v -> showUserDialog(null));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllUsers()
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<User>> call,
                                           @NonNull Response<List<User>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            allUsers = response.body();
                            if (allUsers.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                adapter.setUsers(allUsers);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterUsers(String query) {
        if (query.isEmpty()) {
            adapter.setUsers(allUsers);
            return;
        }
        List<User> filtered = new ArrayList<>();
        String q = query.toLowerCase();
        for (User u : allUsers) {
            if ((u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                filtered.add(u);
            }
        }
        adapter.setUsers(filtered);
    }

    private void showUserDialog(User existing) {
        DialogAdminUserBinding db = DialogAdminUserBinding.inflate(LayoutInflater.from(requireContext()));

        // Role spinner
        String[] roles = {Constants.ROLE_TOURIST, Constants.ROLE_GUIDE, "Company", Constants.ROLE_ADMIN};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        db.spinnerRole.setAdapter(roleAdapter);

        if (existing != null) {
            db.etName.setText(existing.getFullName());
            db.etEmail.setText(existing.getEmail());
            db.etPhone.setText(existing.getPhone());
            db.tilPassword.setHint("Mật khẩu (để trống nếu không đổi)");
            // Select role
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(existing.getRole())) {
                    db.spinnerRole.setSelection(i);
                    break;
                }
            }
        }

        String title = existing == null ? "Tạo người dùng" : "Sửa người dùng";
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(db.getRoot())
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = db.etName.getText() != null ? db.etName.getText().toString().trim() : "";
                    String email = db.etEmail.getText() != null ? db.etEmail.getText().toString().trim() : "";
                    String phone = db.etPhone.getText() != null ? db.etPhone.getText().toString().trim() : "";
                    String password = db.etPassword.getText() != null ? db.etPassword.getText().toString().trim() : "";
                    String role = roles[db.spinnerRole.getSelectedItemPosition()];

                    if (name.isEmpty() || email.isEmpty()) {
                        Toast.makeText(requireContext(), "Tên và email không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existing == null) {
                        if (password.isEmpty()) {
                            Toast.makeText(requireContext(), "Mật khẩu không được trống", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        createUser(name, email, password, phone, role);
                    } else {
                        updateUser(existing.getId(), name, email, phone, password, role);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createUser(String name, String email, String password, String phone, String role) {
        CreateUserRequest request = new CreateUserRequest(name, email, password, phone, role);
        ApiClient.getInstance(requireContext()).getApiService()
                .createUser(request)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Tạo người dùng thành công!", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(requireContext(), "Không thể tạo người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUser(String id, String name, String email, String phone, String password, String role) {
        UpdateProfileRequest request = new UpdateProfileRequest(name, phone);
        request.setEmail(email);
        request.setRole(role);
        if (!password.isEmpty()) request.setPassword(password);

        ApiClient.getInstance(requireContext()).getApiService()
                .updateUser(id, request)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(requireContext(), "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa \"" + user.getFullName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getInstance(requireContext()).getApiService()
                            .deleteUser(user.getId())
                            .enqueue(new Callback<StatusResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<StatusResponse> call,
                                                       @NonNull Response<StatusResponse> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                                        loadUsers();
                                    } else {
                                        Toast.makeText(requireContext(), "Không thể xóa", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

