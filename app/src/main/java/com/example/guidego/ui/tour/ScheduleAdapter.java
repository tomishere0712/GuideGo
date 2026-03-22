package com.example.guidego.ui.tour;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.model.TourSchedule;
import com.example.guidego.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<TourSchedule> schedules = new ArrayList<>();
    private int selectedPosition = -1;
    private OnScheduleSelectedListener listener;

    public interface OnScheduleSelectedListener {
        void onScheduleSelected(TourSchedule schedule);
    }

    public void setSchedules(List<TourSchedule> schedules) {
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setOnScheduleSelectedListener(OnScheduleSelectedListener listener) {
        this.listener = listener;
    }

    public TourSchedule getSelectedSchedule() {
        if (selectedPosition >= 0 && selectedPosition < schedules.size()) {
            TourSchedule s = schedules.get(selectedPosition);
            return isPastDate(s.getStartDate()) ? null : s;
        }
        return null;
    }

    /** Trả về true nếu lịch KHÔNG thể đặt:
     *  - Đã qua (startDate < today), HOẶC
     *  - Là hôm nay (startDate == today) → phải đặt tối thiểu trước 1 ngày.
     */
    private boolean isPastDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            Date scheduleDate = sdf.parse(dateStr);
            Date today = sdf.parse(sdf.format(new Date())); // chuẩn hóa về midnight
            // startDate <= today → không thể đặt (cần đặt trước ít nhất 1 ngày)
            return scheduleDate != null && !scheduleDate.after(today);
        } catch (Exception e) {
            return false;
        }
    }

    /** Trả về true nếu dateStr là ngày hôm nay (để hiện thông báo khác vs ngày đã qua). */
    private boolean isToday(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date()).equals(dateStr);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourSchedule schedule = schedules.get(position);
        boolean isPast = isPastDate(schedule.getStartDate());
        boolean isSelected = position == selectedPosition && !isPast;
        boolean todaySchedule = isToday(schedule.getStartDate());

        // Date range
        holder.tvDateRange.setText(
                FormatUtils.formatDate(schedule.getStartDate())
                        + " → "
                        + FormatUtils.formatDate(schedule.getEndDate()));

        com.google.android.material.card.MaterialCardView card =
                (com.google.android.material.card.MaterialCardView) holder.itemView;

        if (isPast) {
            // --- Lịch không thể đặt (đã qua hoặc hôm nay) ---
            if (todaySchedule) {
                // Hôm nay: hết hạn đặt nhưng tour chưa diễn ra → màu cam nhạt
                holder.tvSlots.setText("Hết hạn đặt chỗ");
                holder.tvSlots.setTextColor(Color.parseColor("#E65100"));
                holder.tvDateRange.setTextColor(Color.parseColor("#E65100"));
                holder.tvPastLabel.setText("Hôm nay");
                holder.tvPastLabel.setTextColor(Color.parseColor("#E65100"));
            } else {
                // Đã qua: grey out hoàn toàn
                holder.tvSlots.setText("Đã qua");
                holder.tvSlots.setTextColor(Color.parseColor("#B0B0B0"));
                holder.tvDateRange.setTextColor(Color.parseColor("#B0B0B0"));
                holder.tvPastLabel.setText("Đã qua");
                holder.tvPastLabel.setTextColor(Color.parseColor("#B0B0B0"));
            }
            holder.ivSelected.setVisibility(View.GONE);
            holder.tvPastLabel.setVisibility(View.VISIBLE);
            card.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            card.setStrokeColor(Color.parseColor("#E0E0E0"));
            card.setStrokeWidth(1);
            card.setAlpha(todaySchedule ? 0.85f : 0.7f);
            holder.itemView.setEnabled(false);
            holder.itemView.setOnClickListener(null);
        } else {
            // --- Lịch hợp lệ (từ ngày mai trở đi) ---
            holder.tvSlots.setText("Còn " + schedule.getAvailableSlots() + " chỗ trống");
            holder.tvSlots.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.colorTextSecondary));
            holder.tvDateRange.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.colorTextPrimary));
            holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.tvPastLabel.setVisibility(View.GONE);
            card.setCardBackgroundColor(Color.WHITE);
            card.setAlpha(1f);
            card.setStrokeColor(isSelected
                    ? ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary)
                    : ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBorder));
            card.setStrokeWidth(isSelected ? 4 : 1);
            holder.itemView.setEnabled(true);
            holder.itemView.setOnClickListener(v -> {
                int prev = selectedPosition;
                selectedPosition = holder.getBindingAdapterPosition();
                notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);
                if (listener != null) listener.onScheduleSelected(schedule);
            });
        }
    }

    @Override
    public int getItemCount() { return schedules.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange, tvSlots, tvPastLabel;
        ImageView ivSelected;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
            tvSlots = itemView.findViewById(R.id.tv_slots);
            ivSelected = itemView.findViewById(R.id.iv_selected);
            tvPastLabel = itemView.findViewById(R.id.tv_past_label);
        }
    }
}
