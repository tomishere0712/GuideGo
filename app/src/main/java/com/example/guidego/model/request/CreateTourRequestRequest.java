package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateTourRequestRequest {
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("location_id")
    private String locationId;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("people_count")
    private int peopleCount;
    @SerializedName("budget_per_person")
    private double budgetPerPerson;
    @SerializedName("preferred_guide_id")
    private String preferredGuideId; // optional

    public CreateTourRequestRequest(String title, String description, String locationId,
                                    String startDate, String endDate,
                                    int peopleCount, double budgetPerPerson) {
        this.title = title;
        this.description = description;
        this.locationId = locationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.peopleCount = peopleCount;
        this.budgetPerPerson = budgetPerPerson;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getPeopleCount() { return peopleCount; }
    public void setPeopleCount(int peopleCount) { this.peopleCount = peopleCount; }
    public double getBudgetPerPerson() { return budgetPerPerson; }
    public void setBudgetPerPerson(double budgetPerPerson) { this.budgetPerPerson = budgetPerPerson; }
    public String getPreferredGuideId() { return preferredGuideId; }
    public void setPreferredGuideId(String preferredGuideId) { this.preferredGuideId = preferredGuideId; }
}

