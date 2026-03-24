package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SuitableGuide {
    @SerializedName("guide_id")
    private String guideId;
    @SerializedName("guide_name")
    private String guideName;
    @SerializedName("languages")
    private List<String> languages;
    @SerializedName("experience_years")
    private int experienceYears;
    @SerializedName("rating")
    private double rating;
    @SerializedName("is_verified")
    private boolean isVerified;

    public String getGuideId() { return guideId; }
    public String getGuideName() { return guideName; }
    public List<String> getLanguages() { return languages; }
    public int getExperienceYears() { return experienceYears; }
    public double getRating() { return rating; }
    public boolean isVerified() { return isVerified; }

    public String getLanguagesDisplay() {
        if (languages == null || languages.isEmpty()) return "";
        return String.join(", ", languages);
    }
}

