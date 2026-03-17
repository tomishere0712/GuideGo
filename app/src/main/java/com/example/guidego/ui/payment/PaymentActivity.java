package com.example.guidego.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.MainActivity;
import com.example.guidego.R;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityPaymentBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.Payment;
import com.example.guidego.model.request.CreatePaymentRequest;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.FormatUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private List<Booking> bookings = new ArrayList<>();
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Parse bookings from intent
        String bookingsJson = getIntent().getStringExtra(Constants.EXTRA_BOOKING_IDS);
        totalAmount = getIntent().getDoubleExtra(Constants.EXTRA_TOTAL_AMOUNT, 0);

        if (bookingsJson != null) {
            Type type = new TypeToken<List<Booking>>(){}.getType();
            bookings = new Gson().fromJson(bookingsJson, type);
        }

        setupBookingSummary();
        binding.tvTotalAmount.setText(FormatUtils.formatVND(totalAmount));
        binding.btnPay.setOnClickListener(v -> processPayment());
    }

    private void setupBookingSummary() {
        BookingSummaryAdapter summaryAdapter = new BookingSummaryAdapter(bookings);
        binding.rvBookingSummary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBookingSummary.setAdapter(summaryAdapter);
    }

    private String getSelectedPaymentMethod() {
        int id = binding.rgPaymentMethod.getCheckedRadioButtonId();
        if (id == R.id.rb_bank_transfer) return Payment.METHOD_BANK_TRANSFER;
        if (id == R.id.rb_credit_card) return Payment.METHOD_CREDIT_CARD;
        return Payment.METHOD_CASH;
    }

    private void processPayment() {
        if (bookings.isEmpty()) return;
        binding.btnPay.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        String method = getSelectedPaymentMethod();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        int total = bookings.size();

        for (Booking booking : bookings) {
            ApiClient.getInstance(this).getApiService()
                    .createPayment(new CreatePaymentRequest(booking.getId(), method))
                    .enqueue(new Callback<Payment>() {
                        @Override
                        public void onResponse(@NonNull Call<Payment> call, @NonNull Response<Payment> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String paymentId = response.body().getId();
                                confirmPayment(paymentId, completed, failed, total);
                            } else {
                                failed.incrementAndGet();
                                checkAllDone(completed, failed, total);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Payment> call, @NonNull Throwable t) {
                            failed.incrementAndGet();
                            checkAllDone(completed, failed, total);
                        }
                    });
        }
    }

    private void confirmPayment(String paymentId, AtomicInteger completed, AtomicInteger failed, int total) {
        ApiClient.getInstance(this).getApiService()
                .confirmPayment(paymentId)
                .enqueue(new Callback<Payment>() {
                    @Override
                    public void onResponse(@NonNull Call<Payment> call, @NonNull Response<Payment> response) {
                        if (response.isSuccessful()) {
                            completed.incrementAndGet();
                        } else {
                            failed.incrementAndGet();
                        }
                        checkAllDone(completed, failed, total);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Payment> call, @NonNull Throwable t) {
                        failed.incrementAndGet();
                        checkAllDone(completed, failed, total);
                    }
                });
    }

    private void checkAllDone(AtomicInteger completed, AtomicInteger failed, int total) {
        if (completed.get() + failed.get() >= total) {
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnPay.setEnabled(true);
                if (failed.get() == 0) {
                    showSuccessDialog();
                } else if (completed.get() > 0) {
                    Toast.makeText(this, "Một số đơn thanh toán thành công", Toast.LENGTH_LONG).show();
                    goToBookings();
                } else {
                    Toast.makeText(this, "Thanh toán thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("✅ " + getString(R.string.payment_success))
                .setMessage(getString(R.string.payment_success_desc))
                .setPositiveButton(getString(R.string.back_to_home), (d, w) -> goToHome())
                .setCancelable(false)
                .show();
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void goToBookings() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Inner adapter for booking summary
    static class BookingSummaryAdapter extends RecyclerView.Adapter<BookingSummaryAdapter.ViewHolder> {
        private final List<Booking> bookings;

        BookingSummaryAdapter(List<Booking> bookings) {
            this.bookings = bookings;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_summary, parent, false);
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

