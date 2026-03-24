package com.example.guidego.ui.guide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.databinding.ItemGuideRequestBinding;
import com.example.guidego.model.Tour;

import java.util.ArrayList;
import java.util.List;

public class GuideRequestAdapter extends RecyclerView.Adapter<GuideRequestAdapter.ViewHolder> {

    public interface OnDecision { void onDecide(Tour tour, boolean accept); }

    private List<Tour> items = new ArrayList<>();
    private final OnDecision listener;

    public GuideRequestAdapter(OnDecision listener) {
        this.listener = listener;
    }

    public void setItems(List<Tour> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemGuideRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        h.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideRequestBinding b;

        ViewHolder(ItemGuideRequestBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Tour tour) {
            Context ctx = b.getRoot().getContext();
            b.tvTitle.setText(tour.getTitle() != null ? tour.getTitle() : "—");

            String loc = tour.getLocationName() != null ? tour.getLocationName()
                    : (tour.getCity() != null ? "📍 " + tour.getCity() : "");
            b.tvRequester.setText(loc);

            // Status
            String status = tour.getGuideRequestStatus();
            if (status == null || status.equals("Pending")) {
                b.tvStatus.setText("Chờ xác nhận");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWarning));
                b.btnAccept.setVisibility(View.VISIBLE);
                b.btnReject.setVisibility(View.VISIBLE);
            } else if (status.equals("Accepted")) {
                b.tvStatus.setText("Đã nhận");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorSuccess));
                b.btnAccept.setVisibility(View.GONE);
                b.btnReject.setVisibility(View.GONE);
            } else {
                b.tvStatus.setText("Đã từ chối");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorError));
                b.btnAccept.setVisibility(View.GONE);
                b.btnReject.setVisibility(View.GONE);
            }

            // Details
            StringBuilder sb = new StringBuilder();
            if (tour.getSchedules() != null && !tour.getSchedules().isEmpty()) {
                sb.append("📅 ").append(tour.getSchedules().get(0).getStartDate())
                        .append(" → ").append(tour.getSchedules().get(0).getEndDate());
            }
            if (tour.getPricePerPerson() > 0) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(String.format("💰 %,.0f đ/người", tour.getPricePerPerson()));
            }
            if (tour.getDescription() != null && !tour.getDescription().isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("📝 ").append(tour.getDescription());
            }
            b.tvDetails.setText(sb.toString());

            b.btnAccept.setOnClickListener(v -> listener.onDecide(tour, true));
            b.btnReject.setOnClickListener(v -> listener.onDecide(tour, false));
        }
    }
}

