package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class AssignGuideRequest {
    @SerializedName("guide_id")
    private String guideId;

    public AssignGuideRequest(String guideId) {
        this.guideId = guideId;
    }

    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }
}

