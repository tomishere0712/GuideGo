package com.example.guidego.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.FragmentCartBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.Cart;
import com.example.guidego.model.CartItem;
import com.example.guidego.model.request.CreateBookingRequest;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.payment.PaymentActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.FormatUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartItemAdapter adapter;
    private Cart currentCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CartItemAdapter();
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        adapter.setOnDeleteListener((item, position) -> deleteCartItem(item, position));
        binding.btnCheckout.setOnClickListener(v -> checkout());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvCart.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);
        binding.bottomCheckout.setVisibility(View.GONE);

        ApiClient.getInstance(requireContext()).getApiService()
                .getCart()
                .enqueue(new Callback<Cart>() {
                    @Override
                    public void onResponse(@NonNull Call<Cart> call, @NonNull Response<Cart> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            currentCart = response.body();
                            displayCart(currentCart);
                        } else if (response.code() == 401) {
                            showEmpty();
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Cart> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        showEmpty();
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            showEmpty();
            return;
        }
        adapter.setItems(cart.getItems());
        binding.rvCart.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
        binding.bottomCheckout.setVisibility(View.VISIBLE);
        binding.tvTotal.setText(FormatUtils.formatVND(cart.getTotalAmount()));
        binding.tvItemCount.setText(cart.getItemCount() + " tour");
    }

    private void showEmpty() {
        binding.rvCart.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.bottomCheckout.setVisibility(View.GONE);
    }

    private void deleteCartItem(CartItem item, int position) {
        ApiClient.getInstance(requireContext()).getApiService()
                .removeFromCart(item.getId())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            adapter.removeItem(position);
                            loadCart(); // Reload to update total
                        } else {
                            Toast.makeText(requireContext(), "Không thể xóa", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        if (isAdded())
                            Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkout() {
        if (currentCart == null) return;
        binding.btnCheckout.setEnabled(false);

        ApiClient.getInstance(requireContext()).getApiService()
                .createBooking(new CreateBookingRequest(currentCart.getCartId()))
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                        if (!isAdded()) return;
                        binding.btnCheckout.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Booking> bookings = response.body();
                            double total = currentCart.getTotalAmount();

                            // Navigate to payment
                            Intent intent = new Intent(requireContext(), PaymentActivity.class);
                            intent.putExtra(Constants.EXTRA_BOOKING_IDS, new Gson().toJson(bookings));
                            intent.putExtra(Constants.EXTRA_TOTAL_AMOUNT, total);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), "Không thể tạo đơn đặt. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.btnCheckout.setEnabled(true);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

