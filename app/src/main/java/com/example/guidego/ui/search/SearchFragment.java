package com.example.guidego.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.FragmentSearchBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.model.TourSearchResponse;
import com.example.guidego.ui.home.TourCardAdapter;
import com.example.guidego.ui.tour.TourDetailActivity;
import com.example.guidego.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private TourCardAdapter adapter;
    private String selectedCity = null;
    private List<Tour> allTours = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new TourCardAdapter(false);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(adapter);

        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { performSearch(); return true; }
            return false;
        });

        // Load cities for filter chips (không load toàn bộ tour ngay)
        loadCitiesForFilter();
    }

    private void loadCitiesForFilter() {
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllTours()
                .enqueue(new Callback<List<Tour>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Tour>> call, @NonNull Response<List<Tour>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            allTours = response.body();
                            setupCityChips(allTours);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {}
                });
    }

    private void setupCityChips(List<Tour> tours) {
        if (!isAdded()) return;
        binding.cityFilterContainer.removeAllViews();
        Set<String> cities = new LinkedHashSet<>();
        for (Tour t : tours) {
            if (t.getCity() != null && !t.getCity().isEmpty()) cities.add(t.getCity());
        }

        addCityChip("Tất cả", null);
        for (String city : cities) addCityChip(city, city);
    }

    private void addCityChip(String label, String city) {
        TextView chip = new TextView(requireContext());
        ViewGroup.MarginLayoutParams p = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(p);
        chip.setText(label);
        chip.setTextSize(13);
        chip.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        updateChipStyle(chip, selectedCity == null && city == null);
        chip.setOnClickListener(v -> {
            selectedCity = city;
            updateAllChipStyles(binding.cityFilterContainer, city);
            performSearch();
        });
        binding.cityFilterContainer.addView(chip);
    }

    private void updateChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            chip.setTextColor(getResources().getColor(R.color.white, null));
        } else {
            chip.setBackgroundResource(R.drawable.bg_city_chip);
            chip.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        }
    }

    private void updateAllChipStyles(ViewGroup container, String selectedCity) {
        List<String> cities = new ArrayList<>();
        cities.add(null);
        for (Tour t : allTours) {
            if (t.getCity() != null && !cities.contains(t.getCity())) cities.add(t.getCity());
        }
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView chip = (TextView) container.getChildAt(i);
            String chipCity = i == 0 ? null : cities.size() > i ? cities.get(i) : null;
            boolean isSelected = (selectedCity == null && chipCity == null) || (selectedCity != null && selectedCity.equals(chipCity));
            updateChipStyle(chip, isSelected);
        }
    }

    private void performSearch() {
        String keyword = binding.etSearch.getText() != null ? binding.etSearch.getText().toString().trim() : "";
        setLoading(true);

        Map<String, Object> params = new HashMap<>();
        if (!keyword.isEmpty()) params.put("keyword", keyword);
        if (selectedCity != null) params.put("city", selectedCity);
        params.put("page", 1);
        params.put("page_size", 20);

        ApiClient.getInstance(requireContext()).getApiService()
                .searchTours(params)
                .enqueue(new Callback<TourSearchResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TourSearchResponse> call, @NonNull Response<TourSearchResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Tour> results = response.body().getItems();
                            showResults(results);
                        } else {
                            showEmpty("Không tìm thấy kết quả phù hợp");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TourSearchResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showResults(List<Tour> results) {
        if (!isAdded()) return;
        if (results == null || results.isEmpty()) {
            showEmpty("Không tìm thấy kết quả");
            return;
        }
        adapter.setTours(results);
        binding.rvSearchResults.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
        binding.tvResultCount.setVisibility(View.VISIBLE);
        binding.tvResultCount.setText("Tìm thấy " + results.size() + " tour");
    }

    private void showEmpty(String msg) {
        if (!isAdded()) return;
        binding.rvSearchResults.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.tvResultCount.setVisibility(View.GONE);
        binding.tvEmptyTitle.setText(msg);
    }

    private void setLoading(boolean loading) {
        if (!isAdded()) return;
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.rvSearchResults.setVisibility(loading ? View.GONE : binding.rvSearchResults.getVisibility());
        binding.emptyState.setVisibility(loading ? View.GONE : binding.emptyState.getVisibility());
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void searchByCity(String city) {
        selectedCity = city;
        performSearch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

