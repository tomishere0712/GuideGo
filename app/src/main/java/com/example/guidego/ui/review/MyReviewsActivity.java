package com.example.guidego.ui.review;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityMyReviewsBinding;
import com.example.guidego.model.Review;
import com.example.guidego.model.request.UpdateReviewRequest;
import com.example.guidego.model.response.StatusResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Screen: "Lịch sử đánh giá" — Tourist views, edits, and deletes their own reviews.
 * Calls GET /api/review/my-reviews
 */
public class MyReviewsActivity extends AppCompatActivity {

    private ActivityMyReviewsBinding binding;
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new ReviewAdapter();
        adapter.setShowActions(true);
        adapter.setShowTourTitle(true);
        adapter.setOnEditListener(this::showEditDialog);
        adapter.setOnDeleteListener(this::confirmDelete);

        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadMyReviews);
        loadMyReviews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyReviews();
    }

    private void loadMyReviews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvReviews.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);

        ApiClient.getInstance(this).getApiService()
                .getMyReviews()
                .enqueue(new Callback<List<Review>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Review>> call,
                                           @NonNull Response<List<Review>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            adapter.setReviews(response.body());
                            binding.rvReviews.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(MyReviewsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showEditDialog(Review review) {
        // Inflate a simple edit dialog with RatingBar + comment EditText
        View dialogView = LayoutInflater.from(this)
                .inflate(com.example.guidego.R.layout.dialog_edit_review, null, false);

        RatingBar ratingBar = dialogView.findViewById(com.example.guidego.R.id.dialog_rating_bar);
        TextInputEditText etComment = dialogView.findViewById(com.example.guidego.R.id.dialog_et_comment);

        ratingBar.setRating(review.getRating());
        if (review.getComment() != null) etComment.setText(review.getComment());

        new AlertDialog.Builder(this)
                .setTitle("Sửa đánh giá")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    int newRating = (int) ratingBar.getRating();
                    String newComment = etComment.getText() != null
                            ? etComment.getText().toString().trim() : "";
                    if (newRating == 0) {
                        Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateReview(review.getId(), newRating, newComment);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateReview(String reviewId, int rating, String comment) {
        ApiClient.getInstance(this).getApiService()
                .updateReview(reviewId, new UpdateReviewRequest(rating, comment))
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MyReviewsActivity.this, "Đã cập nhật đánh giá", Toast.LENGTH_SHORT).show();
                            loadMyReviews();
                        } else if (response.code() == 403) {
                            Toast.makeText(MyReviewsActivity.this, "Không có quyền sửa đánh giá này", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyReviewsActivity.this, "Không thể cập nhật. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MyReviewsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa", (d, w) -> deleteReview(review))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteReview(Review review) {
        ApiClient.getInstance(this).getApiService()
                .deleteReview(review.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MyReviewsActivity.this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                            loadMyReviews();
                        } else if (response.code() == 403) {
                            Toast.makeText(MyReviewsActivity.this, "Không có quyền xóa đánh giá này", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyReviewsActivity.this, "Không thể xóa. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MyReviewsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

