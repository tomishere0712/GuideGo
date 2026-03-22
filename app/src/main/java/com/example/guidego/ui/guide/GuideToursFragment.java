package com.example.guidego.ui.guide;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.guidego.databinding.FragmentGuideToursBinding;
import com.example.guidego.model.Guide;
import com.example.guidego.model.Tour;
import com.example.guidego.model.response.GuidesResponse;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuideToursFragment extends Fragment {

    private FragmentGuideToursBinding binding;
    private TokenManager tokenManager;
    private GuideTourAdapter adapter;
    private List<Tour> allTours = new ArrayList<>();
    private Guide currentGuide;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideToursBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        setupRecyclerView();
        setupSwipeRefresh();
        binding.fabCreate.setOnClickListener(v -> openCreateTour());

        loadGuideProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGuideProfile();
    }

    private void setupRecyclerView() {
        adapter = new GuideTourAdapter(
                tour -> openEditTour(tour),
                tour -> confirmDeleteTour(tour),
                tour -> openManageSchedules(tour)
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadGuideProfile);
    }

    private void loadGuideProfile() {
        String userId = tokenManager.getUserId();
        if (userId.isEmpty()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getGuideByUserId(userId)
                .enqueue(new Callback<GuidesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GuidesResponse> call,
                                           @NonNull Response<GuidesResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null
                                && !response.body().getData().isEmpty()) {
                            currentGuide = response.body().getData().get(0);
                            updateGuideStatusUI();
                            loadMyTours();
                        } else {
                            // No guide profile found
                            binding.progressBar.setVisibility(View.GONE);
                            binding.swipeRefresh.setRefreshing(false);
                            binding.tvGuideStatus.setText("Chưa đăng ký hướng dẫn viên");
                            binding.layoutPendingBanner.setVisibility(View.GONE);
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GuidesResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateGuideStatusUI() {
        if (currentGuide == null) return;
        if (currentGuide.isVerified()) {
            binding.tvGuideStatus.setText("✓ Đã được xác minh");
            binding.layoutPendingBanner.setVisibility(View.GONE);
            binding.fabCreate.setVisibility(View.VISIBLE);
        } else {
            binding.tvGuideStatus.setText("⏳ Chờ Admin duyệt");
            binding.layoutPendingBanner.setVisibility(View.VISIBLE);
            binding.fabCreate.setVisibility(View.GONE);
        }
    }

    private void loadMyTours() {
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllTours()
                .enqueue(new Callback<List<Tour>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Tour>> call,
                                           @NonNull Response<List<Tour>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            // Filter tours that belong to current guide
                            String myUserId = tokenManager.getUserId();
                            allTours.clear();
                            for (Tour t : response.body()) {
                                if (currentGuide != null && currentGuide.getId() != null
                                        && currentGuide.getId().equals(t.getGuideId())) {
                                    allTours.add(t);
                                }
                            }
                            if (allTours.isEmpty()) {
                                showEmpty();
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                adapter.setTours(allTours);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi tải danh sách tour", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmpty() {
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void openCreateTour() {
        Intent intent = new Intent(requireContext(), CreateEditTourActivity.class);
        startActivity(intent);
    }

    private void openEditTour(Tour tour) {
        Intent intent = new Intent(requireContext(), CreateEditTourActivity.class);
        intent.putExtra(Constants.EXTRA_TOUR_ID, tour.getId());
        startActivity(intent);
    }

    private void openManageSchedules(Tour tour) {
        Intent intent = new Intent(requireContext(), ManageSchedulesActivity.class);
        intent.putExtra(Constants.EXTRA_TOUR_ID, tour.getId());
        intent.putExtra("tour_title", tour.getTitle());
        startActivity(intent);
    }

    private void confirmDeleteTour(Tour tour) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa tour")
                .setMessage("Bạn có chắc muốn xóa tour \"" + tour.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTour(tour))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTour(Tour tour) {
        ApiClient.getInstance(requireContext()).getApiService()
                .deleteTour(tour.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đã xóa tour", Toast.LENGTH_SHORT).show();
                            loadGuideProfile();
                        } else {
                            Toast.makeText(requireContext(), "Không thể xóa tour", Toast.LENGTH_SHORT).show();
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

