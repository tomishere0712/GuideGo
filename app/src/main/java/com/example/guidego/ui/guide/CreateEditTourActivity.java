package com.example.guidego.ui.guide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityCreateEditTourBinding;
import com.example.guidego.model.Guide;
import com.example.guidego.model.Location;
import com.example.guidego.model.Tour;
import com.example.guidego.model.request.CreateTourRequest;
import com.example.guidego.model.response.CreateDataResponse;
import com.example.guidego.model.response.GuidesResponse;
import com.example.guidego.model.response.LocationResponse;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEditTourActivity extends AppCompatActivity {

    private ActivityCreateEditTourBinding binding;
    private TokenManager tokenManager;
    private List<Location> locations = new ArrayList<>();
    private List<Guide> guides = new ArrayList<>();
    private String editTourId = null;
    private Uri selectedImageUri = null;
    private boolean isAdminMode = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.ivPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(binding.ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEditTourBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        editTourId = getIntent().getStringExtra(Constants.EXTRA_TOUR_ID);
        isAdminMode = getIntent().getBooleanExtra("admin_mode", false);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPickImage.setOnClickListener(v -> pickImage());
        binding.btnSave.setOnClickListener(v -> saveTour());

        if (isAdminMode) {
            binding.tvGuideLabel.setVisibility(View.VISIBLE);
            binding.spinnerGuide.setVisibility(View.VISIBLE);
            loadGuides();
        }

        if (editTourId != null) {
            binding.tvTitle.setText("Chỉnh sửa Tour");
            loadTourData();
        }

        loadLocations();
    }

    private void loadLocations() {
        ApiClient.getInstance(this).getApiService().getAllLocations()
                .enqueue(new Callback<LocationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationResponse> call,
                                           @NonNull Response<LocationResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            locations = response.body().getData();
                            List<String> names = new ArrayList<>();
                            names.add("-- Chọn địa điểm --");
                            for (Location l : locations) names.add(l.getName());
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    CreateEditTourActivity.this,
                                    android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerLocation.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LocationResponse> call, @NonNull Throwable t) {
                        Toast.makeText(CreateEditTourActivity.this,
                                "Không tải được danh sách địa điểm", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadGuides() {
        ApiClient.getInstance(this).getApiService().getAllGuides()
                .enqueue(new Callback<GuidesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GuidesResponse> call,
                                           @NonNull Response<GuidesResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            guides = response.body().getData();
                            List<String> names = new ArrayList<>();
                            names.add("-- Chọn hướng dẫn viên --");
                            for (Guide g : guides) {
                                String name = g.getUser() != null ? g.getUser().getFullName() : g.getUserId();
                                names.add(name);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    CreateEditTourActivity.this,
                                    android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerGuide.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GuidesResponse> call, @NonNull Throwable t) {}
                });
    }

    private void loadTourData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService().getTourById(editTourId)
                .enqueue(new Callback<Tour>() {
                    @Override
                    public void onResponse(@NonNull Call<Tour> call,
                                           @NonNull Response<Tour> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Tour t = response.body();
                            binding.etTitle.setText(t.getTitle());
                            binding.etDescription.setText(t.getDescription());
                            binding.etPrice.setText(String.valueOf((long) t.getPricePerPerson()));
                            binding.etMaxPeople.setText(String.valueOf(t.getMaxPeople()));
                            binding.etDuration.setText(String.valueOf(t.getDurationDays()));
                            // Select location in spinner
                            for (int i = 0; i < locations.size(); i++) {
                                if (locations.get(i).getId().equals(t.getLocationId())) {
                                    binding.spinnerLocation.setSelection(i + 1);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Tour> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveTour() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
        String maxPeopleStr = binding.etMaxPeople.getText() != null ? binding.etMaxPeople.getText().toString().trim() : "";
        String durationStr = binding.etDuration.getText() != null ? binding.etDuration.getText().toString().trim() : "";

        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()
                || maxPeopleStr.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int locationPos = binding.spinnerLocation.getSelectedItemPosition();
        if (locationPos <= 0 || locationPos > locations.size()) {
            Toast.makeText(this, "Vui lòng chọn địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationId = locations.get(locationPos - 1).getId();
        double price = Double.parseDouble(priceStr);
        int maxPeople = Integer.parseInt(maxPeopleStr);
        int duration = Integer.parseInt(durationStr);

        CreateTourRequest request = new CreateTourRequest(title, description, locationId, price, maxPeople, duration);

        // Admin can assign a guide
        if (isAdminMode && binding.spinnerGuide.getSelectedItemPosition() > 0) {
            int guidePos = binding.spinnerGuide.getSelectedItemPosition();
            request.setGuideId(guides.get(guidePos - 1).getId());
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        if (editTourId == null) {
            // Create
            ApiClient.getInstance(this).getApiService().createTour(request)
                    .enqueue(new Callback<CreateDataResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CreateDataResponse> call,
                                               @NonNull Response<CreateDataResponse> response) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSave.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                String newId = response.body().getData() != null
                                        ? response.body().getData().getId() : null;
                                Toast.makeText(CreateEditTourActivity.this,
                                        "Tạo tour thành công!", Toast.LENGTH_SHORT).show();
                                if (newId != null && selectedImageUri != null) {
                                    uploadImage(newId);
                                } else {
                                    finish();
                                }
                            } else {
                                Toast.makeText(CreateEditTourActivity.this,
                                        "Không thể tạo tour. Hãy kiểm tra lại.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CreateDataResponse> call, @NonNull Throwable t) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSave.setEnabled(true);
                            Toast.makeText(CreateEditTourActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Update
            ApiClient.getInstance(this).getApiService().updateTour(editTourId, request)
                    .enqueue(new Callback<StatusResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<StatusResponse> call,
                                               @NonNull Response<StatusResponse> response) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSave.setEnabled(true);
                            if (response.isSuccessful()) {
                                Toast.makeText(CreateEditTourActivity.this,
                                        "Cập nhật tour thành công!", Toast.LENGTH_SHORT).show();
                                if (selectedImageUri != null) {
                                    uploadImage(editTourId);
                                } else {
                                    finish();
                                }
                            } else {
                                Toast.makeText(CreateEditTourActivity.this,
                                        "Không thể cập nhật tour", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSave.setEnabled(true);
                            Toast.makeText(CreateEditTourActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadImage(String tourId) {
        try {
            String mimeType = getContentResolver().getType(selectedImageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse(mimeType),
                    getFileFromUri(selectedImageUri)
            );
            MultipartBody.Part part = MultipartBody.Part.createFormData("file",
                    "tour_image.jpg", requestBody);

            ApiClient.getInstance(this).getApiService()
                    .uploadTourImage(tourId, part)
                    .enqueue(new Callback<CreateDataResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CreateDataResponse> call,
                                               @NonNull Response<CreateDataResponse> response) {
                            finish();
                        }

                        @Override
                        public void onFailure(@NonNull Call<CreateDataResponse> call,
                                              @NonNull Throwable t) {
                            finish();
                        }
                    });
        } catch (Exception e) {
            finish();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        File file = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
        try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
             java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file)) {
            if (inputStream == null) throw new Exception("Cannot open input stream");
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, len);
        }
        return file;
    }
}

