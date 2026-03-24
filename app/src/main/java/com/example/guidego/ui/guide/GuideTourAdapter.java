package com.example.guidego.ui.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.databinding.ItemGuideTourBinding;
import com.example.guidego.model.Tour;
import com.example.guidego.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class GuideTourAdapter extends RecyclerView.Adapter<GuideTourAdapter.ViewHolder> {

    public interface OnEditClick { void onEdit(Tour tour); }
    public interface OnDeleteClick { void onDelete(Tour tour); }
    public interface OnScheduleClick { void onSchedule(Tour tour); }
    public interface OnBookingsClick { void onBookings(Tour tour); }

    private List<Tour> tours = new ArrayList<>();
    private final OnEditClick editClick;
    private final OnDeleteClick deleteClick;
    private final OnScheduleClick scheduleClick;
    private final OnBookingsClick bookingsClick;

    public GuideTourAdapter(OnEditClick editClick, OnDeleteClick deleteClick,
                             OnScheduleClick scheduleClick, OnBookingsClick bookingsClick) {
        this.editClick = editClick;
        this.deleteClick = deleteClick;
        this.scheduleClick = scheduleClick;
        this.bookingsClick = bookingsClick;
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
            b.tvTitle.setText(tour.getTitle());
            b.tvLocation.setText(tour.getLocationName() != null ? tour.getLocationName() : tour.getCity());
            b.tvPrice.setText(FormatUtils.formatCurrency(tour.getPricePerPerson()));
            b.tvDuration.setText(tour.getDurationDays() + " ngày");
            b.tvMaxPeople.setText(String.valueOf(tour.getMaxPeople()));

            // Status badge: clear tint first, then apply color by status
            b.tvStatus.setBackgroundTintList(null);
            if (tour.isActive()) {
                b.tvStatus.setText("● Hoạt động");
                b.tvStatus.setTextColor(
                        ContextCompat.getColor(b.tvStatus.getContext(), R.color.colorSuccess));
            } else {
                b.tvStatus.setText("○ Ẩn");
                b.tvStatus.setTextColor(
                        ContextCompat.getColor(b.tvStatus.getContext(), R.color.colorTextSecondary));
            }

            b.btnEdit.setOnClickListener(v -> editClick.onEdit(tour));
            b.btnDelete.setOnClickListener(v -> deleteClick.onDelete(tour));
            b.btnSchedule.setOnClickListener(v -> scheduleClick.onSchedule(tour));
            b.btnBookings.setOnClickListener(v -> bookingsClick.onBookings(tour));
        }
    }
}

