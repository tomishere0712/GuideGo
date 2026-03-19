package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Tour {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("location_id")
    private String locationId;
    @SerializedName("location_name")
    private String locationName;
    @SerializedName("city")
    private String city;
    @SerializedName("guide_id")
    private String guideId;
    @SerializedName("guide_name")
    private String guideName;
    @SerializedName("guide_experience_years")
    private int guideExperienceYears;
    @SerializedName("guide_languages")
    private List<String> guideLanguages;
    @SerializedName("guide_is_verified")
    private boolean guideIsVerified;
    @SerializedName("price_per_person")
    private double pricePerPerson;
    @SerializedName("max_people")
    private int maxPeople;
    @SerializedName("duration_days")
    private int durationDays;
    @SerializedName("rating")
    private double rating;
    @SerializedName("is_active")
    private boolean isActive;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("schedules")
    private List<TourSchedule> schedules;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;

    // Helper: get image URL from picsum based on tour id seed
    public String getImageUrl() {
        String seed = (id != null && id.length() >= 8) ? id.substring(0, 8) : "default";
        return "https://picsum.photos/seed/" + seed + "/800/500";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }
    public String getGuideName() { return guideName; }
    public void setGuideName(String guideName) { this.guideName = guideName; }
    public int getGuideExperienceYears() { return guideExperienceYears; }
    public void setGuideExperienceYears(int guideExperienceYears) { this.guideExperienceYears = guideExperienceYears; }
    public List<String> getGuideLanguages() { return guideLanguages; }
    public void setGuideLanguages(List<String> guideLanguages) { this.guideLanguages = guideLanguages; }
    public boolean isGuideIsVerified() { return guideIsVerified; }
    public void setGuideIsVerified(boolean guideIsVerified) { this.guideIsVerified = guideIsVerified; }
    public double getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(double pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public int getMaxPeople() { return maxPeople; }
    public void setMaxPeople(int maxPeople) { this.maxPeople = maxPeople; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public List<TourSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<TourSchedule> schedules) { this.schedules = schedules; }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
