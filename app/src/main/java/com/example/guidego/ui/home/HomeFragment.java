package com.example.guidego.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.guidego.MainActivity;
import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.FragmentHomeBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.utils.FormatUtils;
import com.example.guidego.utils.TokenManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TourCardAdapter featuredAdapter;
    private TourCardAdapter allToursAdapter;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentPage = 0;
    private List<Tour> allTours = new ArrayList<>();
    /** Computed tour-specific averages: key=tourId, value=avg rating from reviews */
    private final java.util.Map<String, Double> tourRatingMap = new java.util.HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bannerHandler = new Handler(Looper.getMainLooper());

        setupGreeting();
        setupBanner();
        setupAdapters();
        setupSearchBar();
        setupSwipeRefresh();
        loadTours();
    }

    private void setupGreeting() {
        TokenManager tm = new TokenManager(requireContext());
        binding.tvGreeting.setText(FormatUtils.getGreeting());
        String name = tm.getUserName();
        binding.tvUserName.setText(name.isEmpty() ? "Bạn ơi" : name);
    }

    private void setupBanner() {
        bannerAdapter = new BannerAdapter();
        binding.vpBanner.setAdapter(bannerAdapter);
        binding.vpBanner.setOffscreenPageLimit(1);

        binding.vpBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateDots(position);
            }
        });
        // Dots and auto-scroll will start after data loads
    }

    private void setupDots(int count) {
        binding.dotsLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            int widthDp = (i == 0) ? 20 : 8;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(widthDp), dpToPx(8));
            params.setMargins(dpToPx(3), 0, dpToPx(3), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundColor(i == 0 ? Color.parseColor("#006AF3") : Color.parseColor("#CCCCCC"));
            binding.dotsLayout.addView(dot);
        }
    }

    private void updateDots(int selected) {
        for (int i = 0; i < binding.dotsLayout.getChildCount(); i++) {
            View dot = binding.dotsLayout.getChildAt(i);
            dot.setBackgroundColor(i == selected ? Color.parseColor("#006AF3") : Color.parseColor("#CCCCCC"));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void startAutoScroll() {
        bannerRunnable = () -> {
            if (bannerAdapter.getItemCount() > 0) {
                currentPage = (currentPage + 1) % bannerAdapter.getItemCount();
                binding.vpBanner.setCurrentItem(currentPage, true);
            }
            bannerHandler.postDelayed(bannerRunnable, 3500);
        };
        bannerHandler.postDelayed(bannerRunnable, 3500);
    }

    private void setupAdapters() {
        // Featured (horizontal)
        featuredAdapter = new TourCardAdapter(true);
        binding.rvFeatured.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeatured.setAdapter(featuredAdapter);

        // All tours (vertical grid)
        allToursAdapter = new TourCardAdapter(false);
        binding.rvTours.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTours.setAdapter(allToursAdapter);
    }

    private void setupSearchBar() {
        binding.searchBar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch();
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        binding.swipeRefresh.setOnRefreshListener(this::loadTours);
    }

    private void loadTours() {
        binding.progressBar.setVisibility(View.VISIBLE);
        fetchTours(true);
    }

    /** Silent refresh — called from onResume, no progress bar, keeps existing data visible */
    private void refreshToursQuietly() {
        fetchTours(false);
    }

    private void fetchTours(boolean showProgress) {
        if (showProgress) binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllTours()
                .enqueue(new Callback<List<Tour>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Tour>> call, @NonNull Response<List<Tour>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            allTours = response.body();
                            // First display with existing rating data, then refresh from reviews
                            displayTours(allTours);
                            fetchAllReviewsAndUpdateRatings();
                        } else if (showProgress) {
                            showError("Không thể tải danh sách tour");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (showProgress) showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /**
     * Fetch all reviews once, group by tour_id, compute per-tour averages,
     * then override tour.rating with the accurate value before re-rendering.
     */
    private void fetchAllReviewsAndUpdateRatings() {
        ApiClient.getInstance(requireContext()).getApiService()
                .getAllReviews()
                .enqueue(new Callback<java.util.List<com.example.guidego.model.Review>>() {
                    @Override
                    public void onResponse(@NonNull Call<java.util.List<com.example.guidego.model.Review>> call,
                                           @NonNull Response<java.util.List<com.example.guidego.model.Review>> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful() || response.body() == null) return;

                        // Group ratings by tour_id
                        java.util.Map<String, java.util.List<Integer>> ratingsByTour = new java.util.HashMap<>();
                        for (com.example.guidego.model.Review r : response.body()) {
                            if (r.getTourId() == null) continue;
                            ratingsByTour.computeIfAbsent(r.getTourId(), k -> new java.util.ArrayList<>())
                                    .add(r.getRating());
                        }

                        // Compute average per tour and update tourRatingMap
                        tourRatingMap.clear();
                        for (java.util.Map.Entry<String, java.util.List<Integer>> entry : ratingsByTour.entrySet()) {
                            double sum = 0;
                            for (int v : entry.getValue()) sum += v;
                            tourRatingMap.put(entry.getKey(), sum / entry.getValue().size());
                        }

                        // Apply computed ratings to tour list and re-render
                        for (Tour t : allTours) {
                            Double computed = tourRatingMap.get(t.getId());
                            if (computed != null) t.setRating(computed);
                            else if (!tourRatingMap.containsKey(t.getId())) t.setRating(0.0);
                        }
                        displayTours(allTours);
                    }

                    @Override
                    public void onFailure(@NonNull Call<java.util.List<com.example.guidego.model.Review>> call,
                                         @NonNull Throwable t) {
                        // silently ignore — tour.rating from API is already displayed
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Silently refresh tour ratings every time the user returns to Home tab
        if (!allTours.isEmpty()) {
            refreshToursQuietly();
        }
    }

    private void displayTours(List<Tour> tours) {
        if (!isAdded()) return;

        // Banner: pick up to 5 tours that have a real image (not picsum fallback)
        List<Tour> bannerTours = new ArrayList<>();
        for (Tour t : tours) {
            String img = t.getImageUrl();
            if (img != null && !img.isEmpty() && !img.contains("picsum.photos")) {
                bannerTours.add(t);
            }
            if (bannerTours.size() >= 5) break;
        }
        // If not enough real images, fill with any tour
        if (bannerTours.size() < 5) {
            for (Tour t : tours) {
                if (!bannerTours.contains(t)) bannerTours.add(t);
                if (bannerTours.size() >= 5) break;
            }
        }
        bannerAdapter.setTours(bannerTours);
        setupDots(bannerAdapter.getItemCount());
        if (bannerHandler != null) bannerHandler.removeCallbacks(bannerRunnable);
        startAutoScroll();

        // Featured: first 6 tours
        List<Tour> featured = tours.size() > 6 ? tours.subList(0, 6) : new ArrayList<>(tours);
        featuredAdapter.setTours(featured);

        // All tours
        allToursAdapter.setTours(tours);

        // Destination chips
        setupDestinationChips(tours);
    }

    private void setupDestinationChips(List<Tour> tours) {
        binding.destinationsContainer.removeAllViews();
        Set<String> cities = new LinkedHashSet<>();
        for (Tour t : tours) {
            if (t.getCity() != null && !t.getCity().isEmpty()) cities.add(t.getCity());
        }

        // "Tất cả" chip first
        addCityChip("Tất cả", null);
        for (String city : cities) {
            addCityChip(city, city);
        }
    }

    private void addCityChip(String label, String cityFilter) {
        TextView chip = new TextView(requireContext());
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);
        chip.setText(label);
        chip.setTextSize(13);
        chip.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        chip.setBackgroundResource(R.drawable.bg_city_chip);
        chip.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        chip.setOnClickListener(v -> {
            if (cityFilter == null) {
                allToursAdapter.setTours(allTours);
            } else {
                List<Tour> filtered = new ArrayList<>();
                for (Tour t : allTours) {
                    if (cityFilter.equals(t.getCity())) filtered.add(t);
                }
                allToursAdapter.setTours(filtered);
            }
        });
        binding.destinationsContainer.addView(chip);
    }

    private void showError(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
        binding = null;
    }
}
