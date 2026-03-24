package com.example.guidego.ui.booking;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.model.Booking;
import com.example.guidego.model.Review;
import com.example.guidego.utils.FormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    /** key = tourTitle (lower-case), value = Review already submitted by this user */
    private Map<String, Review> reviewedMap = new HashMap<>();
    private OnCancelListener listener;
    private OnReviewListener reviewListener;
    private OnEditReviewListener editReviewListener;

    public interface OnCancelListener {
        void onCancel(Booking booking, int position);
    }
    public interface OnReviewListener {
        void onReview(Booking booking);
    }
    public interface OnEditReviewListener {
        void onEditReview(Booking booking, Review existing);
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Call after loadMyReviews() returns so we can show the "Đã đánh giá" state */
    public void setMyReviews(List<Review> reviews) {
        reviewedMap.clear();
        if (reviews != null) {
            for (Review r : reviews) {
                if (r.getTourTitle() != null) {
                    reviewedMap.put(r.getTourTitle().toLowerCase(), r);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < bookings.size()) {
            bookings.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnCancelListener(OnCancelListener listener)             { this.listener = listener; }
    public void setOnReviewListener(OnReviewListener reviewListener)       { this.reviewListener = reviewListener; }
    public void setOnEditReviewListener(OnEditReviewListener l)            { this.editReviewListener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        holder.tvTitle.setText(booking.getTourTitle());
        holder.tvCode.setText("Mã: " + FormatUtils.shortenId(booking.getId()));
        holder.tvDates.setText(FormatUtils.formatDateRange(booking.getStartDate(), booking.getEndDate()));
        holder.tvPeople.setText(booking.getPeopleCount() + " người");
        holder.tvPrice.setText(FormatUtils.formatVND(booking.getTotalPrice()));

        // Status badge
        holder.tvStatus.setText(FormatUtils.getStatusLabel(booking.getStatus()));
        int bgColor;
        switch (booking.getStatus() != null ? booking.getStatus() : "") {
            case Booking.STATUS_CONFIRMED:  bgColor = R.color.statusConfirmed;  break;
            case Booking.STATUS_COMPLETED:  bgColor = R.color.statusCompleted;  break;
            case Booking.STATUS_CANCELLED:  bgColor = R.color.statusCancelled;  break;
            default:                        bgColor = R.color.statusPending;
        }
        holder.tvStatus.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), bgColor));

        // Cancel button (only for Pending)
        if (booking.isCancellable()) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v ->
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Xác nhận hủy đơn")
                            .setMessage("Bạn có muốn hủy đơn đặt này không?")
                            .setPositiveButton("Hủy đơn", (d, w) -> {
                                if (listener != null) listener.onCancel(booking, holder.getAdapterPosition());
                            })
                            .setNegativeButton("Không", null)
                            .show());
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        // Review buttons
        if (booking.isReviewable()) {
            String key = booking.getTourTitle() != null
                    ? booking.getTourTitle().toLowerCase() : "";
            Review existing = reviewedMap.get(key);

            if (existing != null) {
                // Already reviewed → show label + edit button
                holder.btnWriteReview.setVisibility(View.GONE);
                holder.tvReviewedLabel.setVisibility(View.VISIBLE);
                holder.btnEditReview.setVisibility(View.VISIBLE);
                holder.btnEditReview.setOnClickListener(v -> {
                    if (editReviewListener != null) editReviewListener.onEditReview(booking, existing);
                });
            } else {
                // Not yet reviewed → show "Viết đánh giá"
                holder.btnWriteReview.setVisibility(View.VISIBLE);
                holder.tvReviewedLabel.setVisibility(View.GONE);
                holder.btnEditReview.setVisibility(View.GONE);
                holder.btnWriteReview.setOnClickListener(v -> {
                    if (reviewListener != null) reviewListener.onReview(booking);
                });
            }
        } else {
            holder.btnWriteReview.setVisibility(View.GONE);
            holder.tvReviewedLabel.setVisibility(View.GONE);
            holder.btnEditReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCode, tvDates, tvPeople, tvPrice, tvStatus, tvReviewedLabel;
        com.google.android.material.button.MaterialButton btnCancel;
        com.google.android.material.button.MaterialButton btnWriteReview;
        com.google.android.material.button.MaterialButton btnEditReview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tv_tour_title);
            tvCode         = itemView.findViewById(R.id.tv_booking_code);
            tvDates        = itemView.findViewById(R.id.tv_dates);
            tvPeople       = itemView.findViewById(R.id.tv_people);
            tvPrice        = itemView.findViewById(R.id.tv_price);
            tvStatus       = itemView.findViewById(R.id.tv_status);
            tvReviewedLabel = itemView.findViewById(R.id.tv_reviewed_label);
            btnCancel      = itemView.findViewById(R.id.btn_cancel);
            btnWriteReview = itemView.findViewById(R.id.btn_write_review);
            btnEditReview  = itemView.findViewById(R.id.btn_edit_review);
        }
    }
}
