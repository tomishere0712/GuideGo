package com.example.guidego.ui.guide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityGuideBookingsBinding;
import com.example.guidego.databinding.ItemGuideBookingBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.FormatUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuideBookingsActivity extends AppCompatActivity {

    private ActivityGuideBookingsBinding binding;
    private BookingsAdapter adapter;
    private String tourId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tourId = getIntent().getStringExtra(Constants.EXTRA_TOUR_ID);
        String tourTitle = getIntent().getStringExtra("tour_title");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTourName.setText(tourTitle != null ? tourTitle : "");

        adapter = new BookingsAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadBookings);
        loadBookings();
    }

    private void loadBookings() {
        if (tourId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        ApiClient.getInstance(this).getApiService()
                .getBookingsByTour(tourId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call,
                                           @NonNull Response<List<Booking>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            adapter.setBookings(response.body());
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            binding.layoutEmpty.setVisibility(View.GONE);
                        } else {
                            binding.recyclerView.setVisibility(View.GONE);
                            binding.layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(GuideBookingsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmComplete(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hoàn thành")
                .setMessage("Xác nhận tour \"" + booking.getTourTitle() + "\" đã kết thúc?")
                .setPositiveButton("Xác nhận", (d, w) -> completeBooking(booking))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void completeBooking(Booking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .completeBooking(booking.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(GuideBookingsActivity.this,
                                    "Tour đã được đánh dấu hoàn thành!", Toast.LENGTH_SHORT).show();
                            loadBookings();
                        } else if (response.code() == 403) {
                            Toast.makeText(GuideBookingsActivity.this,
                                    "Bạn không có quyền thực hiện thao tác này", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(GuideBookingsActivity.this,
                                    "Tour chưa kết thúc hoặc trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GuideBookingsActivity.this,
                                    "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(GuideBookingsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Inner Adapter ───────────────────────────────────────────────────────

    class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {

        private List<Booking> bookings = new ArrayList<>();

        void setBookings(List<Booking> list) {
            this.bookings = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemGuideBookingBinding b = ItemGuideBookingBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(bookings.get(position));
        }

        @Override
        public int getItemCount() { return bookings.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemGuideBookingBinding b;

            ViewHolder(ItemGuideBookingBinding binding) {
                super(binding.getRoot());
                this.b = binding;
            }

            void bind(Booking booking) {
                b.tvBookingCode.setText("Mã đơn: " + FormatUtils.shortenId(booking.getId()));
                b.tvDates.setText(FormatUtils.formatDateRange(booking.getStartDate(), booking.getEndDate()));
                b.tvPeople.setText(booking.getPeopleCount() + " người");
                b.tvPrice.setText(FormatUtils.formatVND(booking.getTotalPrice()));

                // Status badge
                b.tvStatus.setText(FormatUtils.getStatusLabel(booking.getStatus()));
                int bgColor;
                switch (booking.getStatus() != null ? booking.getStatus() : "") {
                    case Booking.STATUS_CONFIRMED:  bgColor = R.color.statusConfirmed; break;
                    case Booking.STATUS_COMPLETED:  bgColor = R.color.statusCompleted; break;
                    case Booking.STATUS_CANCELLED:  bgColor = R.color.statusCancelled; break;
                    default: bgColor = R.color.statusPending; break;
                }
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(b.getRoot().getContext(), bgColor));

                // "Complete" button: only for Confirmed bookings whose endDate <= today
                if (isCompletable(booking)) {
                    b.btnComplete.setVisibility(View.VISIBLE);
                    b.btnComplete.setOnClickListener(v -> confirmComplete(booking));
                } else {
                    b.btnComplete.setVisibility(View.GONE);
                }
            }

            /** Mirrors the BE rule: Confirmed + endDate <= today */
            private boolean isCompletable(Booking booking) {
                if (!Booking.STATUS_CONFIRMED.equals(booking.getStatus())) return false;
                String endDate = booking.getEndDate();
                if (endDate == null || endDate.isEmpty()) return false;
                try {
                    String datePart = endDate.contains("T") ? endDate.split("T")[0] : endDate;
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                            "yyyy-MM-dd", java.util.Locale.getDefault());
                    java.util.Date end = sdf.parse(datePart);
                    return end != null && !end.after(new java.util.Date());
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }
}

