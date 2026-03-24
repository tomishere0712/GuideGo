package com.example.guidego.ui.guide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.FragmentGuideRequestsBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.model.request.GuideDecisionRequest;
import com.example.guidego.model.response.StatusResponse;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuideTourRequestsFragment extends Fragment {

    private FragmentGuideRequestsBinding binding;
    private GuideRequestAdapter adapter;
    private final List<Tour> allRequests = new ArrayList<>();
    private String currentFilter = "Pending"; // Pending | Accepted | Rejected

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new GuideRequestAdapter(this::handleDecision);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadRequests);

        // Setup filter tabs
        String[] tabs = {"Chờ xác nhận", "Đã nhận", "Đã từ chối"};
        for (String t : tabs) binding.tabLayout.addTab(binding.tabLayout.newTab().setText(t));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1: currentFilter = "Accepted"; break;
                    case 2: currentFilter = "Rejected"; break;
                    default: currentFilter = "Pending"; break;
                }
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadRequests();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }

    private void loadRequests() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Use the new dedicated endpoint — backend filters by authenticated guide automatically
        ApiClient.getInstance(requireContext()).getApiService()
                .getGuideRequests()
                .enqueue(new Callback<List<Tour>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Tour>> call,
                                           @NonNull Response<List<Tour>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            allRequests.clear();
                            for (Tour t : response.body()) {
                                // This screen shows custom requests from tourists only
                                if (t.isCustomRequest()) {
                                    allRequests.add(t);
                                }
                            }
                            applyFilter();
                        } else {
                            binding.layoutEmpty.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyFilter() {
        List<Tour> filtered = new ArrayList<>();
        for (Tour t : allRequests) {
            String status = t.getGuideRequestStatus();
            // Regular (non-custom) tours don't have guide_request_status → treat as "Accepted"
            if (status == null || status.isEmpty()) status = "Accepted";
            if (status.equals(currentFilter)) {
                filtered.add(t);
            }
        }
        adapter.setItems(filtered);
        boolean empty = filtered.isEmpty();
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void handleDecision(Tour tour, boolean accept) {
        String msg = accept ? "Bạn muốn nhận tour này?" : "Bạn muốn từ chối tour này?";
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(accept ? "Nhận tour" : "Từ chối tour")
                .setMessage(msg)
                .setPositiveButton("Xác nhận", (d, w) -> sendDecision(tour, accept))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendDecision(Tour tour, boolean accept) {
        ApiClient.getInstance(requireContext()).getApiService()
                .guideDecision(tour.getId(), new GuideDecisionRequest(accept))
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    accept ? "Đã nhận tour!" : "Đã từ chối tour",
                                    Toast.LENGTH_SHORT).show();
                            loadRequests();
                        } else {
                            Toast.makeText(requireContext(), "Không thể thực hiện", Toast.LENGTH_SHORT).show();
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
