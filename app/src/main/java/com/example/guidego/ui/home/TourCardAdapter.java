package com.example.guidego.ui.home;

import android.content.Intent;
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
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class TourCardAdapter extends RecyclerView.Adapter<TourCardAdapter.ViewHolder> {

    private List<Tour> tours = new ArrayList<>();
    private final boolean isFeatured;

    public TourCardAdapter(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public void setTours(List<Tour> tours) {
        this.tours = tours != null ? tours : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isFeatured ? R.layout.item_tour_featured : R.layout.item_tour_card;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tour tour = tours.get(position);

        Glide.with(holder.itemView.getContext())
                .load(tour.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.bg_placeholder)
                .into(holder.ivImage);

        holder.tvTitle.setText(tour.getTitle());
        holder.tvRating.setText(FormatUtils.formatRating(tour.getRating()));
        holder.tvPrice.setText(FormatUtils.formatVND(tour.getPricePerPerson()));

        if (!isFeatured) {
            if (holder.tvCity != null) holder.tvCity.setText(tour.getCity());
            if (holder.tvLocation != null) holder.tvLocation.setText(tour.getLocationName() != null ? tour.getLocationName() : tour.getCity());
            if (holder.tvDuration != null) holder.tvDuration.setText(tour.getDurationDays() + " ngày");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TourDetailActivity.class);
            intent.putExtra(Constants.EXTRA_TOUR_ID, tour.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return tours.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvRating, tvPrice, tvCity, tvLocation, tvDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_tour_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCity = itemView.findViewById(R.id.tv_city_tag);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }
}

