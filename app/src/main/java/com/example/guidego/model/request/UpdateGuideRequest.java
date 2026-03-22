package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpdateGuideRequest {
    @SerializedName("experience_years")
    private int experienceYears;
    @SerializedName("languages")
    private List<String> languages;
    @SerializedName("description")
    private String description;

    public UpdateGuideRequest(int experienceYears, List<String> languages, String description) {
        this.experienceYears = experienceYears;
        this.languages = languages;
        this.description = description;
    }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

