package com.example.guidego.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.databinding.ItemAdminLocationBinding;
import com.example.guidego.model.Location;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationAdapter extends RecyclerView.Adapter<AdminLocationAdapter.ViewHolder> {

    public interface OnEditClick { void onEdit(Location location); }
    public interface OnDeleteClick { void onDelete(Location location); }

    private List<Location> locations = new ArrayList<>();
    private final OnEditClick editClick;
    private final OnDeleteClick deleteClick;

    public AdminLocationAdapter(OnEditClick editClick, OnDeleteClick deleteClick) {
        this.editClick = editClick;
        this.deleteClick = deleteClick;
    }

    public void setLocations(List<Location> list) {
        this.locations = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminLocationBinding binding = ItemAdminLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(locations.get(position));
    }

    @Override
    public int getItemCount() { return locations.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminLocationBinding b;

        ViewHolder(ItemAdminLocationBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Location loc) {
            b.tvName.setText(loc.getName() != null ? loc.getName() : "—");
            b.tvCity.setText(loc.getCity() != null ? loc.getCity() : "—");
            String address = "";
            if (loc.getAddress() != null && !loc.getAddress().isEmpty()) {
                address = loc.getAddress();
            }
            if (loc.getCountry() != null && !loc.getCountry().isEmpty()) {
                address += (address.isEmpty() ? "" : ", ") + loc.getCountry();
            }
            b.tvAddress.setText(address.isEmpty() ? "—" : address);

            b.btnEdit.setOnClickListener(v -> editClick.onEdit(loc));
            b.btnDelete.setOnClickListener(v -> deleteClick.onDelete(loc));
        }
    }
}

