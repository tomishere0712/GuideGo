package com.example.guidego.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guidego.databinding.ItemAdminUserBinding;
import com.example.guidego.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnEditClick { void onEdit(User user); }
    public interface OnDeleteClick { void onDelete(User user); }

    private List<User> users = new ArrayList<>();
    private final OnEditClick editClick;
    private final OnDeleteClick deleteClick;

    public AdminUserAdapter(OnEditClick editClick, OnDeleteClick deleteClick) {
        this.editClick = editClick;
        this.deleteClick = deleteClick;
    }

    public void setUsers(List<User> list) {
        this.users = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() { return users.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminUserBinding b;

        ViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(User user) {
            b.tvName.setText(user.getFullName() != null ? user.getFullName() : "—");
            b.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
            b.tvRole.setText(user.getRole() != null ? user.getRole() : "—");

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(b.getRoot().getContext()).load(user.getAvatarUrl()).into(b.ivAvatar);
            }

            b.btnEdit.setOnClickListener(v -> editClick.onEdit(user));
            b.btnDelete.setOnClickListener(v -> deleteClick.onDelete(user));
        }
    }
}

