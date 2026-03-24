package com.example.guidego.ui.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.model.Review;
import com.example.guidego.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable adapter for displaying reviews.
 * In read-only mode (Tour Detail, Guide Profile): edit/delete buttons are hidden.
 * In my-reviews mode: edit/delete buttons are shown, tour_title is shown.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    public interface OnEditClick { void onEdit(Review review); }
    public interface OnDeleteClick { void onDelete(Review review); }

    private List<Review> reviews = new ArrayList<>();
    private OnEditClick editListener;
    private OnDeleteClick deleteListener;
    private boolean showActions = false;   // show edit/delete buttons
    private boolean showTourTitle = false; // show tour title (for My Reviews screen)

    public ReviewAdapter() {}

    public void setShowActions(boolean showActions) {
        this.showActions = showActions;
    }

    public void setShowTourTitle(boolean showTourTitle) {
        this.showTourTitle = showTourTitle;
    }

    public void setOnEditListener(OnEditClick listener) {
        this.editListener = listener;
    }

    public void setOnDeleteListener(OnDeleteClick listener) {
        this.deleteListener = listener;
    }

    public void setReviews(List<Review> list) {
        this.reviews = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Review> getReviews() { return reviews; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarLetter, tvReviewerName, tvReviewDate, tvComment, tvTourTitle, tvRatingValue;
        RatingBar ratingBar;
        ImageView btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter  = itemView.findViewById(R.id.tv_avatar_letter);
            tvReviewerName  = itemView.findViewById(R.id.tv_reviewer_name);
            tvReviewDate    = itemView.findViewById(R.id.tv_review_date);
            tvComment       = itemView.findViewById(R.id.tv_comment);
            tvTourTitle     = itemView.findViewById(R.id.tv_tour_title);
            tvRatingValue   = itemView.findViewById(R.id.tv_rating_value);
            ratingBar       = itemView.findViewById(R.id.rating_bar);
            btnEdit         = itemView.findViewById(R.id.btn_edit_review);
            btnDelete       = itemView.findViewById(R.id.btn_delete_review);
        }

        void bind(Review review) {
            // Reviewer name & avatar letter
            String name = review.getFullName() != null && !review.getFullName().isEmpty()
                    ? review.getFullName() : "?";
            tvReviewerName.setText(name);
            tvAvatarLetter.setText(name.substring(0, 1).toUpperCase());

            // Date
            tvReviewDate.setText(FormatUtils.formatDate(review.getCreatedAt()));

            // Rating
            ratingBar.setRating(review.getRating());
            tvRatingValue.setText(review.getRating() + "/5");

            // Comment
            String comment = review.getComment() != null && !review.getComment().isEmpty()
                    ? review.getComment() : "(Không có bình luận)";
            tvComment.setText(comment);

            // Tour title (My Reviews screen)
            if (showTourTitle && review.getTourTitle() != null && !review.getTourTitle().isEmpty()) {
                tvTourTitle.setVisibility(View.VISIBLE);
                tvTourTitle.setText("📍 " + review.getTourTitle());
            } else {
                tvTourTitle.setVisibility(View.GONE);
            }

            // Action buttons
            if (showActions) {
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> {
                    if (editListener != null) editListener.onEdit(review);
                });
                btnDelete.setOnClickListener(v -> {
                    if (deleteListener != null) deleteListener.onDelete(review);
                });
            } else {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
            }
        }
    }
}

