package com.example.guidego.ui.tour;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityTourDetailBinding;
import com.example.guidego.model.ChatRoom;
import com.example.guidego.model.Tour;
import com.example.guidego.model.TourSchedule;
import com.example.guidego.model.request.AddToCartRequest;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.chat.ChatActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.FormatUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TourDetailActivity extends AppCompatActivity{
    private ActivityTourDetailBinding binding;
    private ScheduleAdapter scheduleAdapter;
    private Tour currentTour;
    private int peopleCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTourDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String tourId = getIntent().getStringExtra(Constants.EXTRA_TOUR_ID);
        if (tourId == null) { finish(); return; }

        binding.mapPreview.onCreate(savedInstanceState);

        setupScheduleAdapter();
        setupPeopleCounter();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddToCart.setOnClickListener(v -> addToCart());
        binding.btnOpenMap.setOnClickListener(v -> openGoogleMaps());
        binding.btnChatGuide.setOnClickListener(v -> openChatWithGuide());

        loadTourDetail(tourId);
    }

    @Override
    protected void onResume() { super.onResume(); binding.mapPreview.onResume(); }
    @Override
    protected void onPause() { super.onPause(); binding.mapPreview.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); binding.mapPreview.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); binding.mapPreview.onLowMemory(); }


    private void openGoogleMaps() {
        if (currentTour != null && currentTour.getLatitude() != 0) {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    currentTour.getLatitude(), currentTour.getLongitude(),
                    currentTour.getLatitude(), currentTour.getLongitude(),
                    currentTour.getTitle());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            try {
                startActivity(intent);
            } catch (Exception e) {
                // Mở bằng trình duyệt nếu máy không có app Google Maps
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        } else {
            Toast.makeText(this, "Thông tin vị trí chưa được cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupScheduleAdapter() {
        scheduleAdapter = new ScheduleAdapter();
        binding.rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSchedules.setAdapter(scheduleAdapter);
    }

    private void setupPeopleCounter() {
        binding.tvPeopleCount.setText(String.valueOf(peopleCount));
        binding.btnDecrease.setOnClickListener(v -> {
            if (peopleCount > 1) {
                peopleCount--;
                binding.tvPeopleCount.setText(String.valueOf(peopleCount));
            }
        });
        binding.btnIncrease.setOnClickListener(v -> {
            int max = currentTour != null ? currentTour.getMaxPeople() : 20;
            if (peopleCount < max) {
                peopleCount++;
                binding.tvPeopleCount.setText(String.valueOf(peopleCount));
            }
        });
    }

    private void loadTourDetail(String tourId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .getTourById(tourId)
                .enqueue(new Callback<Tour>() {
                    @Override
                    public void onResponse(Call<Tour> call, Response<Tour> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            currentTour = response.body();
                            displayTour(currentTour);
                            // Fetch fresh schedule data separately to get up-to-date available_slots
                            loadFreshSchedules(tourId);
                        } else {
                            Toast.makeText(TourDetailActivity.this, "Không tìm thấy tour", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Tour> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(TourDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    /** Gọi API lịch tour riêng để lấy available_slots cập nhật nhất */
    private void loadFreshSchedules(String tourId) {
        ApiClient.getInstance(this).getApiService()
                .getTourSchedules(tourId)
                .enqueue(new Callback<List<TourSchedule>>() {
                    @Override
                    public void onResponse(Call<List<TourSchedule>> call, Response<List<TourSchedule>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Cập nhật lịch với dữ liệu tươi nhất từ API tour-schedules
                            scheduleAdapter.setSchedules(response.body());
                            binding.tvNoSchedules.setVisibility(View.GONE);
                        }
                        // Nếu API lỗi → giữ nguyên schedules từ tour response (đã set trước đó)
                    }

                    @Override
                    public void onFailure(Call<List<TourSchedule>> call, Throwable t) {
                        // Không làm gì — schedules từ tour response vẫn được hiển thị
                    }
                });
    }

    private void displayTour(Tour tour) {
        // Image
        Glide.with(this).load(tour.getImageUrl()).centerCrop()
                .placeholder(R.drawable.bg_placeholder).into(binding.ivTourImage);

        // Title & info
        binding.tvTitle.setText(tour.getTitle());
        binding.tvRatingBadge.setText(FormatUtils.formatRating(tour.getRating()) + " ★");
        binding.tvCity.setText(tour.getCity() != null ? tour.getCity() : tour.getLocationName());
        binding.tvDuration.setText(tour.getDurationDays() + " ngày");
        binding.tvMaxPeople.setText("Tối đa " + tour.getMaxPeople());
        binding.tvPrice.setText(FormatUtils.formatVND(tour.getPricePerPerson()));
        binding.tvDescription.setText(tour.getDescription());

        // Guide info
        binding.tvGuideName.setText(tour.getGuideName() != null ? tour.getGuideName() : "Hướng dẫn viên");
        binding.tvGuideExp.setText(tour.getGuideExperienceYears() + " năm kinh nghiệm");
        if (tour.getGuideLanguages() != null) {
            binding.tvGuideLanguages.setText("Ngôn ngữ: " + FormatUtils.joinLanguages(tour.getGuideLanguages()));
        }
        binding.ivVerified.setVisibility(tour.isGuideIsVerified() ? View.VISIBLE : View.GONE);

        // Schedules
        if (tour.getSchedules() != null && !tour.getSchedules().isEmpty()) {
            scheduleAdapter.setSchedules(tour.getSchedules());
            binding.tvNoSchedules.setVisibility(View.GONE);
        } else {
            binding.tvNoSchedules.setVisibility(View.VISIBLE);
        }

        if (tour.getLatitude() !=0) {
            binding.mapPreview.onCreate(null); // Khởi tạo cho Lite Mode
            binding.mapPreview.getMapAsync(googleMap -> {
                LatLng pos = new LatLng(tour.getLatitude(), tour.getLongitude());
                googleMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(pos));
                googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(pos, 15f));

                // Nhấn vào bản đồ nhỏ cũng mở luôn Google Maps lớn
                googleMap.setOnMapClickListener(latLng -> openGoogleMaps());
            });
        }
    }

    private void addToCart() {
        TourSchedule selected = scheduleAdapter.getSelectedSchedule();
        if (selected == null) {
            Toast.makeText(this, "Vui lòng chọn lịch khởi hành", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentTour == null) return;

        binding.btnAddToCart.setEnabled(false);
        ApiClient.getInstance(this).getApiService()
                .addToCart(new AddToCartRequest(currentTour.getId(), selected.getId(), peopleCount))
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                        binding.btnAddToCart.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(TourDetailActivity.this, "✅ Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                            // Refresh lịch để cập nhật available_slots mới nhất
                            loadFreshSchedules(currentTour.getId());
                        } else if (response.code() == 401) {
                            Toast.makeText(TourDetailActivity.this, "Vui lòng đăng nhập để đặt tour", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = "Không thể thêm vào giỏ. Vui lòng thử lại.";
                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    JsonObject json = new Gson().fromJson(errorJson, JsonObject.class);
                                    if (json.has("message")) errorMsg = json.get("message").getAsString();
                                }
                            } catch (Exception ignored) {}
                            new AlertDialog.Builder(TourDetailActivity.this)
                                    .setTitle("Không thể thêm vào giỏ")
                                    .setMessage(errorMsg)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StatusResponse> call, Throwable t) {
                        binding.btnAddToCart.setEnabled(true);
                        Toast.makeText(TourDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openChatWithGuide() {
        if (currentTour == null || currentTour.getGuideId() == null) {
            Toast.makeText(this, "Không tìm thấy thông tin hướng dẫn viên", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnChatGuide.setEnabled(false);
        ApiClient.getInstance(this).getApiService()
                .getOrCreateChat(currentTour.getGuideId())
                .enqueue(new retrofit2.Callback<ChatRoom>() {
                    @Override
                    public void onResponse(retrofit2.Call<ChatRoom> call, retrofit2.Response<ChatRoom> response) {
                        binding.btnChatGuide.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            ChatRoom chat = response.body();
                            Intent intent = new Intent(TourDetailActivity.this, ChatActivity.class);
                            intent.putExtra(Constants.EXTRA_CHAT_ID, chat.getId());
                            intent.putExtra(Constants.EXTRA_CHAT_NAME,
                                    currentTour.getGuideName() != null
                                            ? currentTour.getGuideName() : "Hướng dẫn viên");
                            startActivity(intent);
                        } else if (response.code() == 401) {
                            Toast.makeText(TourDetailActivity.this,
                                    "Vui lòng đăng nhập để chat", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TourDetailActivity.this,
                                    "Không thể mở chat. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ChatRoom> call, Throwable t) {
                        binding.btnChatGuide.setEnabled(true);
                        Toast.makeText(TourDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
