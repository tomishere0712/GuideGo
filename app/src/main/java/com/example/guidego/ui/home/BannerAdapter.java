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

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    private static final String[] IMAGES = {
            "https://picsum.photos/seed/hanoi2024/800/400",
            "https://picsum.photos/seed/danang2024/800/400",
            "https://picsum.photos/seed/hoian2024/800/400",
            "https://picsum.photos/seed/halong2024/800/400",
            "https://picsum.photos/seed/saigon2024/800/400"
    };

    private static final String[] TITLES = {
            "Hà Nội - Thủ đô ngàn năm văn hiến",
            "Đà Nẵng - Thành phố đáng sống",
            "Hội An - Phố cổ di sản thế giới",
            "Hạ Long - Kỳ quan thiên nhiên thế giới",
            "TP. Hồ Chí Minh - Thành phố năng động"
    };

    private static final String[] SUBTITLES = {
            "Khám phá văn hóa và lịch sử",
            "Biển xanh cát trắng nắng vàng",
            "Đèn lồng lung linh quyến rũ",
            "Vịnh nước xanh trong vắt",
            "Ẩm thực và cuộc sống sôi động"
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(IMAGES[position])
                .centerCrop()
                .placeholder(R.drawable.bg_placeholder)
                .into(holder.ivBanner);
        holder.tvDestination.setText(TITLES[position]);
        holder.tvSubtitle.setText(SUBTITLES[position]);
    }

    @Override
    public int getItemCount() { return IMAGES.length; }

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

