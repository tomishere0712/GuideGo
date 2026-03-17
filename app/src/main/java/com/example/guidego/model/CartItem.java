package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("id")
    private String id;
    @SerializedName("tour_id")
    private String tourId;
    @SerializedName("tour_title")
    private String tourTitle;
    @SerializedName("schedule_id")
    private String scheduleId;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("people_count")
    private int peopleCount;
    @SerializedName("price_per_person")
    private double pricePerPerson;
    @SerializedName("line_total")
    private double lineTotal;

    // Helper image url
    public String getImageUrl() {
        String seed = (tourId != null && tourId.length() >= 8) ? tourId.substring(0, 8) : "default";
        return "https://picsum.photos/seed/" + seed + "/400/300";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }
    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getPeopleCount() { return peopleCount; }
    public void setPeopleCount(int peopleCount) { this.peopleCount = peopleCount; }
    public double getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}

