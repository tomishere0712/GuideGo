package com.example.guidego.ui.cart;

import android.app.AlertDialog;
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
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.guidego.utils.TokenManager;

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

        // Check for expired items
        List<CartItem> expiredItems = getExpiredItems(cart.getItems());
        if (!expiredItems.isEmpty()) {
            binding.btnCheckout.setEnabled(false);
            binding.tvExpiredWarning.setVisibility(View.VISIBLE);
            StringBuilder names = new StringBuilder();
            for (CartItem item : expiredItems) {
                names.append("• ").append(item.getTourTitle()).append(" (").append(item.getStartDate()).append(")\n");
            }
            binding.tvExpiredWarning.setText("⚠️ Các tour sau đã hết hạn, vui lòng xóa trước khi thanh toán:\n" + names.toString().trim());
        } else {
            binding.btnCheckout.setEnabled(true);
            binding.tvExpiredWarning.setVisibility(View.GONE);
            // Check if there's already a pending booking for the same schedule
            checkPendingBookings(cart.getItems());
        }
    }

    private List<CartItem> getExpiredItems(List<CartItem> items) {
        List<CartItem> expired = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());
        for (CartItem item : items) {
            try {
                Date startDate = sdf.parse(item.getStartDate());
                Date today = sdf.parse(todayStr);
                // startDate <= today → !startDate.after(today) (đồng bộ với BE: không cho đặt hôm nay)
                if (startDate != null && today != null && !startDate.after(today)) {
                    expired.add(item);
                }
            } catch (ParseException ignored) {}
        }
        return expired;
    }

    private void checkPendingBookings(List<CartItem> cartItems) {
        String userId = new TokenManager(requireContext()).getUserId();
        if (userId.isEmpty()) return;

        Set<String> cartScheduleIds = new HashSet<>();
        for (CartItem item : cartItems) {
            if (item.getScheduleId() != null) cartScheduleIds.add(item.getScheduleId());
        }

        ApiClient.getInstance(requireContext()).getApiService()
                .getBookings(userId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                        if (!isAdded() || binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            boolean hasPending = false;
                            for (Booking booking : response.body()) {
                                if (Booking.STATUS_PENDING.equals(booking.getStatus())
                                        && booking.getScheduleId() != null
                                        && cartScheduleIds.contains(booking.getScheduleId())) {
                                    hasPending = true;
                                    break;
                                }
                            }
                            if (hasPending) {
                                binding.btnCheckout.setEnabled(false);
                                binding.tvExpiredWarning.setVisibility(View.VISIBLE);
                                binding.tvExpiredWarning.setText(
                                        "⚠️ Bạn đã có đơn đặt chờ thanh toán cho tour này.\n" +
                                        "Vui lòng hoàn tất thanh toán hoặc hủy đơn cũ trong mục \"Đơn của tôi\".");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                        // Don't block checkout if API fails
                    }
                });
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

        // Double-check for expired items before calling API
        List<CartItem> expiredItems = getExpiredItems(currentCart.getItems());
        if (!expiredItems.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng xóa các tour đã hết hạn trước khi thanh toán", Toast.LENGTH_LONG).show();
            return;
        }

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

                            // Clear cart items (fire and forget – best effort)
                            clearCartItems();

                            // Navigate to payment
                            Intent intent = new Intent(requireContext(), PaymentActivity.class);
                            intent.putExtra(Constants.EXTRA_BOOKING_IDS, new Gson().toJson(bookings));
                            intent.putExtra(Constants.EXTRA_TOTAL_AMOUNT, total);
                            startActivity(intent);
                        } else {
                            // Parse error message from server
                            String errorMsg = "Không thể tạo đơn đặt. Vui lòng thử lại.";
                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    JsonObject json = new Gson().fromJson(errorJson, JsonObject.class);
                                    if (json.has("message")) {
                                        errorMsg = json.get("message").getAsString();
                                    }
                                }
                            } catch (Exception ignored) {}

                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Không thể đặt tour")
                                    .setMessage(errorMsg)
                                    .setPositiveButton("OK", null)
                                    .show();

                            // Reload cart in case of data mismatch
                            loadCart();
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

    /**
     * Xóa toàn bộ cart items sau khi booking được tạo thành công.
     * Fire-and-forget: không block UI, lỗi bị bỏ qua vì cart sẽ được reload khi onResume.
     */
    private void clearCartItems() {
        if (currentCart == null || currentCart.getItems() == null) return;
        for (CartItem item : currentCart.getItems()) {
            ApiClient.getInstance(requireContext()).getApiService()
                    .removeFromCart(item.getId())
                    .enqueue(new Callback<StatusResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<StatusResponse> call,
                                               @NonNull Response<StatusResponse> response) {
                            // Ignore – cart will be reloaded on resume
                        }

                        @Override
                        public void onFailure(@NonNull Call<StatusResponse> call,
                                              @NonNull Throwable t) {
                            // Ignore – cart will be reloaded on resume
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

