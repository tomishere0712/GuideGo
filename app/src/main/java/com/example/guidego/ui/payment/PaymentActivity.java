package com.example.guidego.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityPaymentBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.request.VnPayRequest;
import com.example.guidego.model.response.VnPayResponse;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.FormatUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private List<Booking> bookings = new ArrayList<>();
    private double totalAmount;

    private final ActivityResultLauncher<Intent> vnPayLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        String bookingsJson = getIntent().getStringExtra(Constants.EXTRA_BOOKING_IDS);
        totalAmount = getIntent().getDoubleExtra(Constants.EXTRA_TOTAL_AMOUNT, 0);

        if (bookingsJson != null) {
            Type type = new TypeToken<List<Booking>>(){}.getType();
            bookings = new Gson().fromJson(bookingsJson, type);
        }

        setupBookingSummary();
        binding.tvTotalAmount.setText(FormatUtils.formatVND(totalAmount));
        binding.btnPay.setOnClickListener(v -> processVnPayPayment());
    }

    private void setupBookingSummary() {
        BookingSummaryAdapter summaryAdapter = new BookingSummaryAdapter(bookings);
        binding.rvBookingSummary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBookingSummary.setAdapter(summaryAdapter);
    }

    private void processVnPayPayment() {
        if (bookings.isEmpty()) return;
        binding.btnPay.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        List<String> bookingIds = bookings.stream()
                .map(Booking::getId)
                .collect(Collectors.toList());

        ApiClient.getInstance(this).getApiService()
                .createVnPayUrl(new VnPayRequest(bookingIds))
                .enqueue(new Callback<VnPayResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VnPayResponse> call,
                                           @NonNull Response<VnPayResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnPay.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getPaymentUrl() != null) {
                            openVnPayWebView(response.body().getPaymentUrl());
                        } else {
                            String errorMsg = "Không thể tạo link thanh toán. Vui lòng thử lại.";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBodyStr = response.errorBody().string();
                                    JsonObject json = JsonParser.parseString(errorBodyStr).getAsJsonObject();
                                    if (json.has("message")) {
                                        errorMsg = json.get("message").getAsString();
                                    }
                                }
                            } catch (Exception ignored) {}
                            Toast.makeText(PaymentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VnPayResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnPay.setEnabled(true);
                        Toast.makeText(PaymentActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openVnPayWebView(String paymentUrl) {
        Intent intent = new Intent(this, VnPayActivity.class);
        intent.putExtra(Constants.EXTRA_VNPAY_URL, paymentUrl);
        intent.putExtra(Constants.EXTRA_TOTAL_AMOUNT, totalAmount);
        vnPayLauncher.launch(intent);
    }

    // Inner adapter for booking summary
    static class BookingSummaryAdapter extends RecyclerView.Adapter<BookingSummaryAdapter.ViewHolder> {
        private final List<Booking> bookings;

        BookingSummaryAdapter(List<Booking> bookings) { this.bookings = bookings; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_summary, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking b = bookings.get(position);
            holder.tvTitle.setText(b.getTourTitle());
            holder.tvDates.setText(FormatUtils.formatDateRange(b.getStartDate(), b.getEndDate()));
            holder.tvPeople.setText(b.getPeopleCount() + " người");
            holder.tvPrice.setText(FormatUtils.formatVND(b.getTotalPrice()));
        }

        @Override
        public int getItemCount() { return bookings.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDates, tvPeople, tvPrice;
            ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_tour_title);
                tvDates = v.findViewById(R.id.tv_dates);
                tvPeople = v.findViewById(R.id.tv_people);
                tvPrice = v.findViewById(R.id.tv_price);
            }
        }
    }
}

