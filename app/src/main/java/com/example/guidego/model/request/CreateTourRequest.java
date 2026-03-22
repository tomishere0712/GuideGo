package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateTourRequest {
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("location_id")
    private String locationId;
    @SerializedName("guide_id")
    private String guideId; // optional — Admin only
    @SerializedName("price_per_person")
    private double pricePerPerson;
    @SerializedName("max_people")
    private int maxPeople;
    @SerializedName("duration_days")
    private int durationDays;

    public CreateTourRequest(String title, String description, String locationId, double pricePerPerson, int maxPeople, int durationDays) {
        this.title = title;
        this.description = description;
        this.locationId = locationId;
        this.pricePerPerson = pricePerPerson;
        this.maxPeople = maxPeople;
        this.durationDays = durationDays;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }
    public double getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public int getMaxPeople() { return maxPeople; }
    public void setMaxPeople(int maxPeople) { this.maxPeople = maxPeople; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
}

