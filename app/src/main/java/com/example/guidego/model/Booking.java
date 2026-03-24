package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Booking {
    @SerializedName("id")
    private String id;
    @SerializedName(value = "userId", alternate = {"user_id"})
    private String userId;
    @SerializedName(value = "tourId", alternate = {"tour_id"})
    private String tourId;
    @SerializedName(value = "scheduleId", alternate = {"schedule_id"})
    private String scheduleId;
    @SerializedName(value = "tourTitle", alternate = {"tour_title"})
    private String tourTitle;
    @SerializedName(value = "startDate", alternate = {"start_date"})
    private String startDate;
    @SerializedName(value = "endDate", alternate = {"end_date"})
    private String endDate;
    @SerializedName(value = "peopleCount", alternate = {"people_count"})
    private int peopleCount;
    @SerializedName(value = "totalPrice", alternate = {"total_price"})
    private double totalPrice;
    @SerializedName("status")
    private String status;
    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;

    // Status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getPeopleCount() { return peopleCount; }
    public void setPeopleCount(int peopleCount) { this.peopleCount = peopleCount; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** Tourist can cancel only Pending bookings. */
    public boolean isCancellable() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Tourist can write a review when:
     * - status = Completed, OR
     * - status = Confirmed AND endDate <= today (tour already ended)
     */
    public boolean isReviewable() {
        if (STATUS_COMPLETED.equals(status)) return true;
        if (!STATUS_CONFIRMED.equals(status)) return false;
        if (endDate == null || endDate.isEmpty()) return false;
        try {
            String datePart = endDate.contains("T") ? endDate.split("T")[0] : endDate;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date end = sdf.parse(datePart);
            return end != null && !end.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
