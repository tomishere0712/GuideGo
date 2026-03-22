package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateScheduleRequest {
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("available_slots")
    private int availableSlots;

    public UpdateScheduleRequest(String startDate, String endDate, int availableSlots) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.availableSlots = availableSlots;
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }
}

