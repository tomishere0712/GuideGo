package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Field names phải khớp với .NET DTO property names (camelCase).
 * CreateGuideDto: UserId, ExperienceYears, Languages, Description
 */
public class RegisterGuideRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("experience_years")
    private int experienceYears;
    @SerializedName("languages")
    private List<String> languages;
    @SerializedName("description")
    private String description;

    public RegisterGuideRequest(String userId, int experienceYears, List<String> languages, String description) {
        this.userId = userId;
        this.experienceYears = experienceYears;
        this.languages = languages;
        this.description = description;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
