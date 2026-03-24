package com.example.guidego.ui.booking;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityWriteReviewBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.model.request.CreateReviewRequest;
import com.example.guidego.model.request.UpdateReviewRequest;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends AppCompatActivity {

    private ActivityWriteReviewBinding binding;
    private String tourId;
    private String tourTitle;
    private String reviewId;   // non-null → edit mode
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tourId    = getIntent().getStringExtra(Constants.EXTRA_TOUR_ID);
        tourTitle = getIntent().getStringExtra("tour_title");
        reviewId  = getIntent().getStringExtra("review_id");
        isEditMode = reviewId != null && !reviewId.isEmpty();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTourTitle.setText(tourTitle != null ? tourTitle : "");

        // Pre-fill in edit mode
        if (isEditMode) {
            int existingRating = getIntent().getIntExtra("existing_rating", 0);
            String existingComment = getIntent().getStringExtra("existing_comment");
            binding.ratingBar.setRating(existingRating);
            if (existingComment != null) binding.etComment.setText(existingComment);
            binding.btnSubmit.setText("Cập nhật đánh giá");
        }

        binding.btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) binding.ratingBar.getRating();
        String comment = binding.etComment.getText() != null
                ? binding.etComment.getText().toString().trim() : "";

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            return;
        }

        // EDIT MODE — call PUT
        if (isEditMode) {
            doUpdateReview(reviewId, rating, comment);
            return;
        }

        // CREATE MODE — need tourId
        if (tourId != null && !tourId.isEmpty()) {
            doCreateReview(tourId, rating, comment);
            return;
        }

        // Fallback: resolve tourId from tour list by title
        if (tourTitle == null || tourTitle.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin tour", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmit.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getInstance(this).getApiService()
                .getAllTours()
                .enqueue(new Callback<List<Tour>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Tour>> call,
                                           @NonNull Response<List<Tour>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (Tour tour : response.body()) {
                                if (tourTitle.equalsIgnoreCase(tour.getTitle())) {
                                    tourId = tour.getId();
                                    doCreateReview(tourId, rating, comment);
                                    return;
                                }
                            }
                        }
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(WriteReviewActivity.this,
                                "Không tìm thấy thông tin tour", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void doCreateReview(String resolvedTourId, int rating, String comment) {
        binding.btnSubmit.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .createReview(new CreateReviewRequest(resolvedTourId, rating, comment))
                .enqueue(buildReviewCallback("Đánh giá của bạn đã được gửi!"));
    }

    private void doUpdateReview(String id, int rating, String comment) {
        binding.btnSubmit.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .updateReview(id, new UpdateReviewRequest(rating, comment))
                .enqueue(buildReviewCallback("Đã cập nhật đánh giá!"));
    }

    private Callback<StatusResponse> buildReviewCallback(String successMsg) {
        return new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call,
                                   @NonNull Response<StatusResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(WriteReviewActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Không thể gửi đánh giá. Vui lòng thử lại";
                    try {
                        if (response.errorBody() != null) {
                            String json = response.errorBody().string();
                            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
                            if (obj != null && obj.has("message")) {
                                String raw = obj.get("message").getAsString();
                                if (raw.contains("already reviewed")) {
                                    errorMsg = "Bạn đã đánh giá tour này rồi";
                                } else if (raw.contains("completed") || raw.contains("confirmed")) {
                                    errorMsg = "Bạn chưa hoàn thành tour này hoặc tour chưa kết thúc";
                                } else if (raw.contains("Tour not found") || raw.contains("no guide")) {
                                    errorMsg = "Không tìm thấy tour hoặc tour chưa có hướng dẫn viên";
                                } else if (raw.contains("not authorized")) {
                                    errorMsg = "Bạn không có quyền sửa đánh giá này";
                                } else {
                                    errorMsg = raw;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(WriteReviewActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);
                Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
