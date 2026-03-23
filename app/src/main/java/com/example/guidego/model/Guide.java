package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Guide {
    @SerializedName("id")
    private String id;

    @SerializedName(value = "user_id", alternate = {"userId"})
    private String userId;

    @SerializedName(value = "experience_years", alternate = {"experienceYears"})
    private int experienceYears;

    @SerializedName(value = "full_name", alternate = {"fullName"})
    private String fullName;

    @SerializedName("languages")
    private List<String> languages;

    @SerializedName("description")
    private String description;

    @SerializedName("rating")
    private double rating;

    @SerializedName(value = "is_verified", alternate = {"isVerified"})
    private boolean isVerified;

    @SerializedName("user")
    private User user;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
