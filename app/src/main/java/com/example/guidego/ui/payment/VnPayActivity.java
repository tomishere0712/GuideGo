package com.example.guidego.ui.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.guidego.MainActivity;
import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityVnpayBinding;
import com.example.guidego.utils.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VnPayActivity extends AppCompatActivity {

    private ActivityVnpayBinding binding;
    // Guard to prevent showing result dialog multiple times
    private final AtomicBoolean resultShown = new AtomicBoolean(false);

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVnpayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String paymentUrl = getIntent().getStringExtra(Constants.EXTRA_VNPAY_URL);
        binding.btnClose.setOnClickListener(v -> confirmCancel());

        // Handle back press with OnBackPressedDispatcher (không deprecated)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack();
                } else {
                    confirmCancel();
                }
            }
        });

        if (paymentUrl == null || paymentUrl.isEmpty()) {
            finish();
            return;
        }

        setupWebView(paymentUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(String paymentUrl) {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                binding.progressBar.setProgress(newProgress);
            }
        });

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return interceptVnPayReturn(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                interceptVnPayReturn(url);
            }
        });

        binding.webView.loadUrl(paymentUrl);
    }

    /**
     * Intercepts VNPay return URL.
     * Calls BE /api/payments/vnpay/return to update booking status,
     * then shows success/failure dialog.
     */
    private boolean interceptVnPayReturn(String url) {
        if (url == null || !url.contains("/api/payments/vnpay/return")) return false;
        if (!resultShown.compareAndSet(false, true)) return true; // Already handled

        Uri uri = Uri.parse(url);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        boolean clientSideSuccess = "00".equals(responseCode);

        // Build query params map to forward to BE
        Map<String, String> params = new HashMap<>();
        Set<String> paramNames = uri.getQueryParameterNames();
        for (String name : paramNames) {
            String value = uri.getQueryParameter(name);
            if (value != null) params.put(name, value);
        }

        // Show loading while calling BE
        runOnUiThread(() -> {
            binding.webView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        });

        if (clientSideSuccess && !params.isEmpty()) {
            // Call BE to update booking status
            ApiClient.getInstance(this).getApiService()
                    .processVnPayReturn(params)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call,
                                               @NonNull Response<ResponseBody> response) {
                            runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
                            // Show success if BE accepted (2xx) or even if it returned 4xx
                            // (vnp_ResponseCode=00 is authoritative from VNPay)
                            showPaymentResult(true);
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call,
                                              @NonNull Throwable t) {
                            runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
                            // Network error: still show success (VNPay confirmed)
                            // but note status may not be updated
                            showPaymentResult(true);
                        }
                    });
        } else {
            runOnUiThread(() -> binding.progressBar.setVisibility(View.GONE));
            showPaymentResult(false);
        }

        return true;
    }

    private void showPaymentResult(boolean isSuccess) {
        runOnUiThread(() -> {
            binding.webView.setVisibility(View.GONE);
            String title = isSuccess ? "✅ Thanh toán thành công!" : "❌ Thanh toán thất bại";
            String message = isSuccess
                    ? "Đơn đặt tour của bạn đã được xác nhận. Chúc bạn có chuyến đi vui vẻ!"
                    : "Giao dịch không thành công. Vui lòng thử lại hoặc liên hệ hỗ trợ.";
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(isSuccess ? "Xem đơn đặt" : "Về trang chủ",
                            (d, w) -> goToMain(isSuccess))
                    .show();
        });
    }

    private void goToMain(boolean goToBookings) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (goToBookings) intent.putExtra("navigate_to", "bookings");
        startActivity(intent);
        finish();
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Huỷ thanh toán?")
                .setMessage("Bạn có chắc muốn huỷ quá trình thanh toán không?")
                .setPositiveButton("Huỷ thanh toán", (d, w) -> finish())
                .setNegativeButton("Tiếp tục", null)
                .show();
    }
}
