package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {
    @SerializedName("tour_id")
    private String tourId;
    @SerializedName("schedule_id")
    private String scheduleId;
    @SerializedName("people_count")
    private int peopleCount;

    public AddToCartRequest(String tourId, String scheduleId, int peopleCount) {
        this.tourId = tourId;
        this.scheduleId = scheduleId;
        this.peopleCount = peopleCount;
    }

    public String getTourId() { return tourId; }
    public String getScheduleId() { return scheduleId; }
    public int getPeopleCount() { return peopleCount; }
}

