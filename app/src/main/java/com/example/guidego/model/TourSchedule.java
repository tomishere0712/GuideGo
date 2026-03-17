package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TourSchedule {
    @SerializedName("id")
    private String id;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("available_slots")
    private int availableSlots;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    @Override
    public String toString() {
        return startDate + " → " + endDate + " (" + availableSlots + " chỗ)";
    }
}

