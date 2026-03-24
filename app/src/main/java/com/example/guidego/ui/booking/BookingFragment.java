package com.example.guidego.ui.booking;

import android.content.Intent;
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
import com.example.guidego.databinding.FragmentBookingBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.Review;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.tour.MyTourRequestsActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingFragment extends Fragment {

    private FragmentBookingBinding binding;
    private BookingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new BookingAdapter();
        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(adapter);

        adapter.setOnCancelListener((booking, position) -> cancelBooking(booking, position));

        // Open WriteReviewActivity in CREATE mode
        adapter.setOnReviewListener(booking -> {
            Intent intent = new Intent(requireContext(), WriteReviewActivity.class);
            intent.putExtra(Constants.EXTRA_TOUR_ID, booking.getTourId());
            intent.putExtra("tour_title", booking.getTourTitle());
            intent.putExtra("schedule_id", booking.getScheduleId());
            intent.putExtra("booking_id", booking.getId());
            startActivity(intent);
        });

        // Open WriteReviewActivity in EDIT mode
        adapter.setOnEditReviewListener((booking, existing) -> {
            Intent intent = new Intent(requireContext(), WriteReviewActivity.class);
            intent.putExtra(Constants.EXTRA_TOUR_ID, booking.getTourId());
            intent.putExtra("tour_title", booking.getTourTitle());
            intent.putExtra("schedule_id", booking.getScheduleId());
            intent.putExtra("booking_id", booking.getId());
            // Edit mode extras
            intent.putExtra("review_id", existing.getId());
            intent.putExtra("existing_rating", existing.getRating());
            intent.putExtra("existing_comment", existing.getComment());
            startActivity(intent);
        });

        binding.btnMyRequests.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyTourRequestsActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvBookings.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        String userId = new TokenManager(requireContext()).getUserId();
        if (userId.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
            return;
        }

        ApiClient.getInstance(requireContext()).getApiService()
                .getBookings(userId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call,
                                           @NonNull Response<List<Booking>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            adapter.setBookings(response.body());
                            binding.rvBookings.setVisibility(View.VISIBLE);
                            // Also load reviews to reflect "Đã đánh giá" state
                            loadMyReviews();
                        } else {
                            binding.emptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Fetch tourist's own reviews and update adapter so reviewed bookings show correct state */
    private void loadMyReviews() {
        ApiClient.getInstance(requireContext()).getApiService()
                .getMyReviews()
                .enqueue(new Callback<List<Review>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Review>> call,
                                           @NonNull Response<List<Review>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setMyReviews(response.body());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                        // silently ignore — review state just won't be shown
                    }
                });
    }

    private void cancelBooking(Booking booking, int position) {
        ApiClient.getInstance(requireContext()).getApiService()
                .cancelBooking(booking.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Đã hủy đơn thành công", Toast.LENGTH_SHORT).show();
                            loadBookings();
                        } else {
                            Toast.makeText(requireContext(), "Không thể hủy đơn", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (isAdded())
                            Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
