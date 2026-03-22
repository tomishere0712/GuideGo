package com.example.guidego.ui.guide;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.databinding.ItemManageScheduleBinding;
import com.example.guidego.model.TourSchedule;

import java.util.ArrayList;
import java.util.List;

public class ManageScheduleAdapter extends RecyclerView.Adapter<ManageScheduleAdapter.ViewHolder> {

    public interface OnEditClick { void onEdit(TourSchedule schedule); }
    public interface OnDeleteClick { void onDelete(TourSchedule schedule); }

    private List<TourSchedule> schedules = new ArrayList<>();
    private final OnEditClick editClick;
    private final OnDeleteClick deleteClick;

    public ManageScheduleAdapter(OnEditClick editClick, OnDeleteClick deleteClick) {
        this.editClick = editClick;
        this.deleteClick = deleteClick;
    }

    public void setSchedules(List<TourSchedule> list) {
        this.schedules = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManageScheduleBinding binding = ItemManageScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(schedules.get(position));
    }

    @Override
    public int getItemCount() { return schedules.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemManageScheduleBinding b;

        ViewHolder(ItemManageScheduleBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(TourSchedule s) {
            b.tvDateRange.setText(s.getStartDate() + " → " + s.getEndDate());
            b.tvSlots.setText(s.getAvailableSlots() + " chỗ còn lại");
            b.btnEdit.setOnClickListener(v -> editClick.onEdit(s));
            b.btnDelete.setOnClickListener(v -> deleteClick.onDelete(s));
        }
    }
}

