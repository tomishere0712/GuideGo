package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateScheduleRequest {
    @SerializedName("tour_id")
    private String tourId;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("available_slots")
    private int availableSlots;

    public CreateScheduleRequest(String tourId, String startDate, String endDate, int availableSlots) {
        this.tourId = tourId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.availableSlots = availableSlots;
    }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }
}

