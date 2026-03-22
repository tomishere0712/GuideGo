package com.example.guidego.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ItemAdminGuideBinding;
import com.example.guidego.model.Guide;
import com.example.guidego.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminGuideAdapter extends RecyclerView.Adapter<AdminGuideAdapter.ViewHolder> {

    public interface OnVerifyClick { void onVerify(Guide guide); }
    public interface OnRejectClick { void onReject(Guide guide); }

    private List<Guide> guides = new ArrayList<>();
    private final OnVerifyClick verifyClick;
    private final OnRejectClick rejectClick;

    public AdminGuideAdapter(OnVerifyClick verifyClick, OnRejectClick rejectClick) {
        this.verifyClick = verifyClick;
        this.rejectClick = rejectClick;
    }

    public void setGuides(List<Guide> list) {
        this.guides = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminGuideBinding binding = ItemAdminGuideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(guides.get(position));
    }

    @Override
    public int getItemCount() { return guides.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminGuideBinding b;

        ViewHolder(ItemAdminGuideBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Guide guide) {
            Context ctx = b.getRoot().getContext();

            // Rating & experience
            b.tvRating.setText(String.format("%.1f ★", guide.getRating()));
            b.tvExperience.setText(guide.getExperienceYears() + " năm KN");

            // Verified badge
            if (guide.isVerified()) {
                b.tvStatus.setText("Đã duyệt");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorSuccess));
                b.btnVerify.setVisibility(View.GONE);
            } else {
                b.tvStatus.setText("Chờ duyệt");
                b.tvStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWarning));
                b.btnVerify.setVisibility(View.VISIBLE);
            }

            // Display user info
            if (guide.getUser() != null) {
                applyUserInfo(guide.getUser());
            } else if (guide.getUserId() != null && !guide.getUserId().isEmpty()) {
                // Placeholder while loading
                b.tvName.setText("Đang tải...");
                b.tvEmail.setText(guide.getUserId().length() > 8
                        ? guide.getUserId().substring(0, 8) + "..." : guide.getUserId());
                // Fetch user info asynchronously
                ApiClient.getInstance(ctx).getApiService()
                        .getUserById(guide.getUserId())
                        .enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(@NonNull Call<User> call,
                                                   @NonNull Response<User> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    User user = response.body();
                                    guide.setUser(user);
                                    applyUserInfo(user);
                                } else {
                                    b.tvName.setText("HDV #" + (guide.getUserId().length() > 6
                                            ? guide.getUserId().substring(0, 6) : guide.getUserId()));
                                    b.tvEmail.setText("—");
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                                b.tvName.setText("HDV (lỗi tải)");
                                b.tvEmail.setText("—");
                            }
                        });
            } else {
                b.tvName.setText("Hướng dẫn viên");
                b.tvEmail.setText("—");
            }

            b.btnVerify.setOnClickListener(v -> verifyClick.onVerify(guide));
            b.btnReject.setOnClickListener(v -> rejectClick.onReject(guide));
        }

        private void applyUserInfo(User user) {
            b.tvName.setText(user.getFullName() != null && !user.getFullName().isEmpty()
                    ? user.getFullName() : "—");
            b.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(b.getRoot().getContext())
                        .load(user.getAvatarUrl())
                        .circleCrop()
                        .into(b.ivAvatar);
            }
        }
    }
}
