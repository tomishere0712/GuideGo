package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Guide {
    @SerializedName("id")
    private String id;
    @SerializedName("userId")
    private String userId;
    @SerializedName("experienceYears")
    private int experienceYears;
    @SerializedName("languages")
    private List<String> languages;
    @SerializedName("description")
    private String description;
    @SerializedName("rating")
    private double rating;
    @SerializedName("isVerified")
    private boolean isVerified;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}

