package com.example.guidego.ui.admin;

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
import com.example.guidego.databinding.FragmentAdminToursBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.guide.CreateEditTourActivity;
import com.example.guidego.ui.guide.ManageSchedulesActivity;
import com.example.guidego.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminToursFragment extends Fragment {

    private FragmentAdminToursBinding binding;
    private AdminTourAdapter adapter;
    private List<Tour> allTours = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminToursBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminTourAdapter(
                tour -> openEditTour(tour),
                tour -> confirmDelete(tour),
                tour -> openSchedules(tour)
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadTours);
        binding.fabCreate.setOnClickListener(v -> openCreateTour());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTours(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadTours();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTours();
    }

    private void loadTours() {
        binding.progressBar.setVisibility(View.VISIBLE);
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
                            allTours = response.body();
                            if (allTours.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                adapter.setTours(allTours);
                            }
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

    private void filterTours(String query) {
        if (query.isEmpty()) { adapter.setTours(allTours); return; }
        List<Tour> filtered = new ArrayList<>();
        String q = query.toLowerCase();
        for (Tour t : allTours) {
            if ((t.getTitle() != null && t.getTitle().toLowerCase().contains(q))
                    || (t.getCity() != null && t.getCity().toLowerCase().contains(q))) {
                filtered.add(t);
            }
        }
        adapter.setTours(filtered);
    }

    private void openCreateTour() {
        Intent intent = new Intent(requireContext(), CreateEditTourActivity.class);
        intent.putExtra("admin_mode", true);
        startActivity(intent);
    }

    private void openEditTour(Tour tour) {
        Intent intent = new Intent(requireContext(), CreateEditTourActivity.class);
        intent.putExtra(Constants.EXTRA_TOUR_ID, tour.getId());
        intent.putExtra("admin_mode", true);
        startActivity(intent);
    }

    private void openSchedules(Tour tour) {
        Intent intent = new Intent(requireContext(), ManageSchedulesActivity.class);
        intent.putExtra(Constants.EXTRA_TOUR_ID, tour.getId());
        intent.putExtra("tour_title", tour.getTitle());
        startActivity(intent);
    }

    private void confirmDelete(Tour tour) {
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
                            loadTours();
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

