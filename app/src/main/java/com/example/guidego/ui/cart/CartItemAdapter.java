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

import java.util.ArrayList;
import java.util.List;

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.bg_placeholder)
                .into(holder.ivImage);

        holder.tvTitle.setText(item.getTourTitle());
        holder.tvDates.setText(FormatUtils.formatDateRange(item.getStartDate(), item.getEndDate()));
        holder.tvPeople.setText(item.getPeopleCount() + " người");
        holder.tvPrice.setText(FormatUtils.formatVND(item.getLineTotal()));

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có muốn xóa tour này khỏi giỏ hàng?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        if (listener != null) listener.onDelete(item, holder.getAdapterPosition());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDates, tvPeople, tvPrice;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_tour_image);
            tvTitle = itemView.findViewById(R.id.tv_tour_title);
            tvDates = itemView.findViewById(R.id.tv_dates);
            tvPeople = itemView.findViewById(R.id.tv_people);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}

