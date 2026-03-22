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
import com.example.guidego.databinding.FragmentAdminGuidesBinding;
import com.example.guidego.model.Guide;
import com.example.guidego.model.response.GuidesResponse;
import com.example.guidego.model.response.StatusResponse;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminGuidesFragment extends Fragment {

    private FragmentAdminGuidesBinding binding;
    private AdminGuideAdapter adapter;
    private List<Guide> allGuides = new ArrayList<>();
    private int currentTab = 0; // 0=All, 1=Pending, 2=Verified

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminGuidesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminGuideAdapter(
                guide -> confirmVerify(guide),
                guide -> confirmReject(guide)
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadGuides);

        // Setup tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tất cả"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chờ duyệt"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã duyệt"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadGuides();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGuides();
    }

    private void loadGuides() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllGuides()
                .enqueue(new Callback<GuidesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GuidesResponse> call,
                                           @NonNull Response<GuidesResponse> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            allGuides = response.body().getData();
                            applyFilter();
                        } else {
                            Toast.makeText(requireContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
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

    private void applyFilter() {
        List<Guide> filtered = new ArrayList<>();
        for (Guide g : allGuides) {
            if (currentTab == 0) filtered.add(g);
            else if (currentTab == 1 && !g.isVerified()) filtered.add(g);
            else if (currentTab == 2 && g.isVerified()) filtered.add(g);
        }
        if (filtered.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            adapter.setGuides(filtered);
        }
    }

    private void confirmVerify(Guide guide) {
        String name = guide.getUser() != null ? guide.getUser().getFullName() : "hướng dẫn viên này";
        new AlertDialog.Builder(requireContext())
                .setTitle("Duyệt hướng dẫn viên")
                .setMessage("Bạn có chắc muốn duyệt " + name + "?")
                .setPositiveButton("Duyệt", (dialog, which) -> verifyGuide(guide))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void verifyGuide(Guide guide) {
        ApiClient.getInstance(requireContext()).getApiService()
                .verifyGuide(guide.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đã duyệt thành công!", Toast.LENGTH_SHORT).show();
                            loadGuides();
                        } else {
                            Toast.makeText(requireContext(), "Không thể duyệt", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmReject(Guide guide) {
        String name = guide.getUser() != null ? guide.getUser().getFullName() : "hướng dẫn viên này";
        new AlertDialog.Builder(requireContext())
                .setTitle("Từ chối hướng dẫn viên")
                .setMessage("Bạn có chắc muốn từ chối " + name + "? Hành động này sẽ xóa hồ sơ Guide.")
                .setPositiveButton("Từ chối", (dialog, which) -> rejectGuide(guide))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void rejectGuide(Guide guide) {
        ApiClient.getInstance(requireContext()).getApiService()
                .rejectGuide(guide.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đã từ chối.", Toast.LENGTH_SHORT).show();
                            loadGuides();
                        } else {
                            Toast.makeText(requireContext(), "Không thể từ chối", Toast.LENGTH_SHORT).show();
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

