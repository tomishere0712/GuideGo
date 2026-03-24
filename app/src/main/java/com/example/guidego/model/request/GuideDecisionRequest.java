package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class GuideDecisionRequest {
    @SerializedName("accept")
    private boolean accept;

    public GuideDecisionRequest(boolean accept) {
        this.accept = accept;
    }

    public boolean isAccept() { return accept; }
    public void setAccept(boolean accept) { this.accept = accept; }
}

