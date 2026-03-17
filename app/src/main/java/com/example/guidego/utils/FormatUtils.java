package com.example.guidego.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormatUtils {

    private static final NumberFormat VND_FORMAT;

    static {
        VND_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        VND_FORMAT.setMaximumFractionDigits(0);
    }

    public static String formatVND(double amount) {
        return VND_FORMAT.format(amount) + " ₫";
    }

    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            // Handle yyyy-MM-dd
            if (isoDate.length() == 10) {
                SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFmt.parse(isoDate);
                return outputFmt.format(date);
            }
            // Handle ISO 8601
            String cleaned = isoDate.replace("Z", "+00:00");
            if (cleaned.contains("T")) {
                SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String datePart = cleaned.split("T")[0];
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = df.parse(datePart);
                return outputFmt.format(date);
            }
            return isoDate;
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String formatDateRange(String startDate, String endDate) {
        return formatDate(startDate) + " - " + formatDate(endDate);
    }

    public static String formatRating(double rating) {
        return String.format(Locale.getDefault(), "%.1f", rating);
    }

    public static String joinLanguages(List<String> languages) {
        if (languages == null || languages.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < languages.size(); i++) {
            sb.append(languages.get(i));
            if (i < languages.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public static String getStatusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "Pending": return "Chờ xác nhận";
            case "Confirmed": return "Đã xác nhận";
            case "Completed": return "Hoàn thành";
            case "Cancelled": return "Đã hủy";
            default: return status;
        }
    }

    public static String getPaymentMethodLabel(String method) {
        if (method == null) return "";
        switch (method) {
            case "Cash": return "Tiền mặt";
            case "BankTransfer": return "Chuyển khoản";
            case "CreditCard": return "Thẻ tín dụng";
            default: return method;
        }
    }

    public static String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào buổi sáng,";
        else if (hour < 18) return "Chào buổi chiều,";
        else return "Chào buổi tối,";
    }

    public static String shortenId(String id) {
        if (id == null || id.length() < 8) return id;
        return id.substring(0, 8).toUpperCase();
    }
}

