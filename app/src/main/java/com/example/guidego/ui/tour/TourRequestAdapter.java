package com.example.guidego.ui.tour;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.databinding.ItemTourRequestBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.Tour;
import com.example.guidego.model.TourSchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourRequestAdapter extends RecyclerView.Adapter<TourRequestAdapter.ViewHolder> {

    public interface OnAssignGuideClick { void onClick(Tour tour); }
    public interface OnBookNowClick    { void onClick(Tour tour); }
    /** Called when tourist taps "Thanh toán ngay" on a tour with an existing Pending booking. */
    public interface OnPayBookingClick { void onClick(Booking booking); }

    private List<Tour> items = new ArrayList<>();
    private OnAssignGuideClick assignClick;
    private OnBookNowClick bookNowClick;
    private OnPayBookingClick payBookingClick;
    /**
     * Maps scheduleId → the best non-cancelled Booking for that schedule.
     * Confirmed/Completed takes priority over Pending.
     */
    private Map<String, Booking> scheduleBookingMap = new HashMap<>();

    public TourRequestAdapter(OnAssignGuideClick assignClick) {
        this.assignClick = assignClick;
    }

    public void setOnBookNowClick(OnBookNowClick listener) {
        this.bookNowClick = listener;
    }

    public void setOnPayBookingClick(OnPayBookingClick listener) {
        this.payBookingClick = listener;
    }

    /** Update schedule→booking map and refresh the list. */
    public void setScheduleBookingMap(Map<String, Booking> map) {
        this.scheduleBookingMap = map != null ? map : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setItems(List<Tour> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTourRequestBinding b = ItemTourRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTourRequestBinding b;

        ViewHolder(ItemTourRequestBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Tour tour) {
            Context ctx = b.getRoot().getContext();

            b.tvTitle.setText(tour.getTitle() != null ? tour.getTitle() : "—");

            String loc = tour.getLocationName() != null ? tour.getLocationName()
                    : (tour.getCity() != null ? tour.getCity() : "—");
            b.tvLocation.setText("📍 " + loc);

            // Status badge
            String status = tour.getGuideRequestStatus();
            if (status == null) status = "Pending";
            switch (status) {
                case "Accepted":
                    b.tvStatus.setText("✅ Đã xác nhận");
                    b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorSuccess));
                    break;
                case "Rejected":
                    b.tvStatus.setText("❌ Đã từ chối");
                    b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorError));
                    break;
                default:
                    b.tvStatus.setText("⏳ Chờ xác nhận");
                    b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWarning));
                    break;
            }

            // Dates from schedules
            if (tour.getSchedules() != null && !tour.getSchedules().isEmpty()) {
                String start = tour.getSchedules().get(0).getStartDate();
                String end   = tour.getSchedules().get(0).getEndDate();
                b.tvDates.setText("📅 " + start + " → " + end);
            } else {
                b.tvDates.setText("📅 Chưa có lịch");
            }

            // Guide
            String guide = tour.getGuideName();
            b.tvGuide.setText("👤 " + (guide != null && !guide.isEmpty()
                    ? guide : "Chưa có hướng dẫn viên"));

            // Budget / price
            if (tour.getPricePerPerson() > 0) {
                b.tvBudget.setText(String.format("%,.0f đ/người", tour.getPricePerPerson()));
            } else {
                b.tvBudget.setText("Ngân sách: Thỏa thuận");
            }

            // Find the best active booking for this tour's schedules (match by scheduleId)
            Booking activeBooking = null;
            if (tour.getSchedules() != null) {
                for (TourSchedule s : tour.getSchedules()) {
                    Booking candidate = scheduleBookingMap.get(s.getId());
                    if (candidate != null) {
                        // Prefer Confirmed/Completed over Pending
                        if (activeBooking == null) {
                            activeBooking = candidate;
                        } else if ((Booking.STATUS_CONFIRMED.equals(candidate.getStatus())
                                || Booking.STATUS_COMPLETED.equals(candidate.getStatus()))
                                && Booking.STATUS_PENDING.equals(activeBooking.getStatus())) {
                            activeBooking = candidate;
                        }
                    }
                }
            }

            boolean accepted = "Accepted".equals(tour.getGuideRequestStatus());

            b.btnAssignGuide.setVisibility(!accepted ? View.VISIBLE : View.GONE);
            b.btnAssignGuide.setOnClickListener(v -> assignClick.onClick(tour));

            b.btnBookNow.setVisibility(accepted ? View.VISIBLE : View.GONE);

            if (activeBooking != null && (Booking.STATUS_CONFIRMED.equals(activeBooking.getStatus())
                    || Booking.STATUS_COMPLETED.equals(activeBooking.getStatus()))) {
                // Already paid — disable button
                b.btnBookNow.setText("Đã thanh toán");
                b.btnBookNow.setEnabled(false);
                b.btnBookNow.setAlpha(0.55f);
                b.btnBookNow.setOnClickListener(null);
            } else if (activeBooking != null && Booking.STATUS_PENDING.equals(activeBooking.getStatus())) {
                // Booking exists but not paid yet — offer to pay
                b.btnBookNow.setText("Thanh toán ngay");
                b.btnBookNow.setEnabled(true);
                b.btnBookNow.setAlpha(1.0f);
                final Booking pendingBooking = activeBooking;
                b.btnBookNow.setOnClickListener(v -> {
                    if (payBookingClick != null) payBookingClick.onClick(pendingBooking);
                });
            } else {
                // No active booking — allow new booking
                b.btnBookNow.setText("Đặt ngay");
                b.btnBookNow.setEnabled(true);
                b.btnBookNow.setAlpha(1.0f);
                b.btnBookNow.setOnClickListener(v -> {
                    if (bookNowClick != null) bookNowClick.onClick(tour);
                });
            }
        }
    }
}
