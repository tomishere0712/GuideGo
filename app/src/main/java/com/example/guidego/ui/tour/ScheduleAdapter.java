package com.example.guidego.ui.tour;

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

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<TourSchedule> schedules = new ArrayList<>();
    private int selectedPosition = -1;
    private OnScheduleSelectedListener listener;

    public interface OnScheduleSelectedListener {
        void onScheduleSelected(TourSchedule schedule);
    }

    public void setSchedules(List<TourSchedule> schedules) {
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnScheduleSelectedListener(OnScheduleSelectedListener listener) {
        this.listener = listener;
    }

    public TourSchedule getSelectedSchedule() {
        if (selectedPosition >= 0 && selectedPosition < schedules.size()) {
            return schedules.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourSchedule schedule = schedules.get(position);
        boolean isSelected = position == selectedPosition;

        holder.tvDateRange.setText(FormatUtils.formatDate(schedule.getStartDate()) + " → " + FormatUtils.formatDate(schedule.getEndDate()));
        holder.tvSlots.setText("Còn " + schedule.getAvailableSlots() + " chỗ trống");
        holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Highlight selected
        com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) holder.itemView;
        card.setStrokeColor(isSelected ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary) :
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBorder));
        card.setStrokeWidth(isSelected ? 4 : 1);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onScheduleSelected(schedule);
        });
    }

    @Override
    public int getItemCount() { return schedules.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange, tvSlots;
        ImageView ivSelected;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
            tvSlots = itemView.findViewById(R.id.tv_slots);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }
    }
}

