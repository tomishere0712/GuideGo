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
import com.example.guidego.databinding.FragmentAdminReviewsBinding;
import com.example.guidego.model.Review;
import com.example.guidego.model.response.StatusResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReviewsFragment extends Fragment {

    private FragmentAdminReviewsBinding binding;
    private AdminReviewAdapter adapter;
    private List<Review> reviews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminReviewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminReviewAdapter(review -> confirmDelete(review));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadReviews);
        loadReviews();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReviews();
    }

    private void loadReviews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getMyReviews()
                .enqueue(new Callback<List<Review>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Review>> call,
                                           @NonNull Response<List<Review>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            reviews = response.body();
                            binding.tvCount.setText(reviews.size() + " đánh giá");
                            if (reviews.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                adapter.setReviews(reviews);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Không tải được đánh giá", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(Review review) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteReview(review))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteReview(Review review) {
        ApiClient.getInstance(requireContext()).getApiService()
                .deleteReview(review.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                            loadReviews();
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
    }
}

