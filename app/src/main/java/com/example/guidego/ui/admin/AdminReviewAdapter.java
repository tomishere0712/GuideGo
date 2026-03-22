package com.example.guidego.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.databinding.ItemAdminReviewBinding;
import com.example.guidego.model.Review;

import java.util.ArrayList;
import java.util.List;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {

    public interface OnDeleteClick { void onDelete(Review review); }

    private List<Review> reviews = new ArrayList<>();
    private final OnDeleteClick deleteClick;

    public AdminReviewAdapter(OnDeleteClick deleteClick) {
        this.deleteClick = deleteClick;
    }

    public void setReviews(List<Review> list) {
        this.reviews = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminReviewBinding binding = ItemAdminReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminReviewBinding b;

        ViewHolder(ItemAdminReviewBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Review review) {
            int rating = review.getRating();
            // Build star string safely (String.repeat() needs API 33+)
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < Math.min(rating, 5); i++) stars.append("⭐");
            b.tvRating.setText(rating + " / 5  " + stars.toString());
            b.tvComment.setText(review.getComment() != null && !review.getComment().isEmpty()
                    ? review.getComment() : "(Không có bình luận)");

            String meta = "Tour: " + (review.getTourId() != null
                    ? review.getTourId().substring(0, Math.min(8, review.getTourId().length())) + "..."
                    : "—");
            if (review.getCreatedAt() != null && review.getCreatedAt().length() >= 10) {
                meta += "  •  " + review.getCreatedAt().substring(0, 10);
            }
            b.tvMeta.setText(meta);

            b.btnDelete.setOnClickListener(v -> deleteClick.onDelete(review));
        }
    }
}

