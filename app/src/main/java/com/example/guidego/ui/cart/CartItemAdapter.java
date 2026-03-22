package com.example.guidego.ui.cart;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guidego.R;
import com.example.guidego.model.CartItem;
import com.example.guidego.utils.FormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private List<CartItem> items = new ArrayList<>();
    private OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(CartItem item, int position);
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }

    private boolean isExpired(CartItem item) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(item.getStartDate());
            Date today = sdf.parse(sdf.format(new Date()));
            // startDate <= today → !startDate.after(today) (đồng bộ với BE)
            return startDate != null && !startDate.after(today);
        } catch (ParseException e) {
            return false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        boolean expired = isExpired(item);

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.bg_placeholder)
                .into(holder.ivImage);

        holder.tvTitle.setText(item.getTourTitle());
        holder.tvDates.setText(FormatUtils.formatDateRange(item.getStartDate(), item.getEndDate()));
        holder.tvPeople.setText(item.getPeopleCount() + " người");
        holder.tvPrice.setText(FormatUtils.formatVND(item.getLineTotal()));

        // Show/hide expired badge and dim card
        if (expired) {
            holder.tvExpiredLabel.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(0.65f);
        } else {
            holder.tvExpiredLabel.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);
        }

        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có muốn xóa tour này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (d, w) -> {
                    if (listener != null) listener.onDelete(item, holder.getBindingAdapterPosition());
                })
                .setNegativeButton("Hủy", null)
                .show());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDates, tvPeople, tvPrice, tvExpiredLabel;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_tour_image);
            tvTitle = itemView.findViewById(R.id.tv_tour_title);
            tvDates = itemView.findViewById(R.id.tv_dates);
            tvPeople = itemView.findViewById(R.id.tv_people);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvExpiredLabel = itemView.findViewById(R.id.tv_expired_label);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}

