package com.example.guidego.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.databinding.ItemGuideTourBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminTourAdapter extends RecyclerView.Adapter<AdminTourAdapter.ViewHolder> {

    public interface OnEditClick { void onEdit(Tour tour); }
    public interface OnDeleteClick { void onDelete(Tour tour); }
    public interface OnScheduleClick { void onSchedule(Tour tour); }

    private List<Tour> tours = new ArrayList<>();
    private final OnEditClick editClick;
    private final OnDeleteClick deleteClick;
    private final OnScheduleClick scheduleClick;

    public AdminTourAdapter(OnEditClick editClick, OnDeleteClick deleteClick,
                             OnScheduleClick scheduleClick) {
        this.editClick = editClick;
        this.deleteClick = deleteClick;
        this.scheduleClick = scheduleClick;
    }

    public void setTours(List<Tour> list) {
        this.tours = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideTourBinding binding = ItemGuideTourBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tours.get(position));
    }

    @Override
    public int getItemCount() { return tours.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideTourBinding b;

        ViewHolder(ItemGuideTourBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Tour tour) {
            Context ctx = b.getRoot().getContext();
            b.tvTitle.setText(tour.getTitle());
            String loc = tour.getLocationName() != null ? tour.getLocationName() : tour.getCity();
            b.tvLocation.setText(loc != null ? loc : "—");
            b.tvPrice.setText(FormatUtils.formatCurrency(tour.getPricePerPerson()));
            b.tvDuration.setText(tour.getDurationDays() + " ngày");
            b.tvMaxPeople.setText(String.valueOf(tour.getMaxPeople()));
            
            // Status with color coding
            if (tour.isActive()) {
                b.tvStatus.setText("Hoạt động");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, com.example.guidego.R.color.colorSuccess));
            } else {
                b.tvStatus.setText("Ẩn");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, com.example.guidego.R.color.colorError));
            }

            // Show guide name if available
            String guideLabel = tour.getGuideName() != null ? "HDV: " + tour.getGuideName() : "";
            // Reuse status chip to show HDV name (subtle override):
            if (!guideLabel.isEmpty()) b.tvLocation.setText(loc + " • " + guideLabel);

            b.btnEdit.setOnClickListener(v -> editClick.onEdit(tour));
            b.btnDelete.setOnClickListener(v -> deleteClick.onDelete(tour));
            b.btnSchedule.setOnClickListener(v -> scheduleClick.onSchedule(tour));
        }
    }
}

