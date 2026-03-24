package com.example.guidego.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guidego.R;
import com.example.guidego.model.Tour;
import com.example.guidego.ui.tour.TourDetailActivity;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    private List<Tour> tours = new ArrayList<>();

    public void setTours(List<Tour> data) {
        this.tours = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tour tour = tours.get(position);

        // Load actual tour image
        Glide.with(holder.itemView.getContext())
                .load(tour.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.bg_placeholder)
                .error(R.drawable.bg_placeholder)
                .into(holder.ivBanner);

        // Title = tour name
        holder.tvDestination.setText(tour.getTitle() != null ? tour.getTitle() : "");

        // Subtitle = city or location
        String sub = tour.getCity() != null && !tour.getCity().isEmpty()
                ? tour.getCity()
                : (tour.getLocationName() != null ? tour.getLocationName() : "");
        holder.tvSubtitle.setText(sub);

        // Tap banner → open tour detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TourDetailActivity.class);
            intent.putExtra("tour_id", tour.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return tours.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvDestination, tvSubtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_banner);
            tvDestination = itemView.findViewById(R.id.tv_destination);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
        }
    }
}
