package com.example.guidego.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.DialogAdminLocationBinding;
import com.example.guidego.databinding.FragmentAdminLocationsBinding;
import com.example.guidego.model.Location;
import com.example.guidego.model.request.CreateLocationRequest;
import com.example.guidego.model.response.CreateDataResponse;
import com.example.guidego.model.response.LocationResponse;
import com.example.guidego.model.response.StatusResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLocationsFragment extends Fragment {

    private FragmentAdminLocationsBinding binding;
    private AdminLocationAdapter adapter;
    private List<Location> locations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminLocationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminLocationAdapter(
                loc -> showLocationDialog(loc),
                loc -> confirmDelete(loc)
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadLocations);
        binding.fabCreate.setOnClickListener(v -> showLocationDialog(null));

        loadLocations();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLocations();
    }

    private void loadLocations() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllLocations()
                .enqueue(new Callback<LocationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationResponse> call,
                                           @NonNull Response<LocationResponse> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            locations = response.body().getData();
                            binding.tvCount.setText(locations.size() + " địa điểm");
                            if (locations.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                adapter.setLocations(locations);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LocationResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLocationDialog(Location existing) {
        DialogAdminLocationBinding db = DialogAdminLocationBinding.inflate(
                LayoutInflater.from(requireContext()));

        if (existing != null) {
            db.etName.setText(existing.getName());
            db.etCity.setText(existing.getCity());
            db.etAddress.setText(existing.getAddress());
            db.etCountry.setText(existing.getCountry() != null ? existing.getCountry() : "Vietnam");
            db.etLatitude.setText(String.valueOf(existing.getLatitude()));
            db.etLongitude.setText(String.valueOf(existing.getLongitude()));
        } else {
            db.etCountry.setText("Vietnam");
        }

        String title = existing == null ? "Thêm địa điểm" : "Sửa địa điểm";
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(db.getRoot())
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = db.etName.getText() != null ? db.etName.getText().toString().trim() : "";
                    String city = db.etCity.getText() != null ? db.etCity.getText().toString().trim() : "";
                    String address = db.etAddress.getText() != null ? db.etAddress.getText().toString().trim() : "";
                    String country = db.etCountry.getText() != null ? db.etCountry.getText().toString().trim() : "Vietnam";
                    String latStr = db.etLatitude.getText() != null ? db.etLatitude.getText().toString().trim() : "0";
                    String lngStr = db.etLongitude.getText() != null ? db.etLongitude.getText().toString().trim() : "0";

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Tên địa điểm không được trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double lat = 0, lng = 0;
                    try { lat = Double.parseDouble(latStr); } catch (NumberFormatException ignored) {}
                    try { lng = Double.parseDouble(lngStr); } catch (NumberFormatException ignored) {}

                    CreateLocationRequest request = new CreateLocationRequest(name, lat, lng, address, city, country);
                    if (existing == null) {
                        createLocation(request);
                    } else {
                        updateLocation(existing.getId(), request);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createLocation(CreateLocationRequest request) {
        ApiClient.getInstance(requireContext()).getApiService()
                .createLocation(request)
                .enqueue(new Callback<CreateDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateDataResponse> call,
                                           @NonNull Response<CreateDataResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Thêm địa điểm thành công!", Toast.LENGTH_SHORT).show();
                            loadLocations();
                        } else {
                            Toast.makeText(requireContext(), "Không thể thêm địa điểm", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CreateDataResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLocation(String id, CreateLocationRequest request) {
        ApiClient.getInstance(requireContext()).getApiService()
                .updateLocation(id, request)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            loadLocations();
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

    private void confirmDelete(Location location) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa địa điểm")
                .setMessage("Bạn có chắc muốn xóa \"" + location.getName() + "\"?\nLưu ý: Không thể xóa nếu đang có tour sử dụng địa điểm này.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getInstance(requireContext()).getApiService()
                            .deleteLocation(location.getId())
                            .enqueue(new Callback<StatusResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<StatusResponse> call,
                                                       @NonNull Response<StatusResponse> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Đã xóa địa điểm", Toast.LENGTH_SHORT).show();
                                        loadLocations();
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

