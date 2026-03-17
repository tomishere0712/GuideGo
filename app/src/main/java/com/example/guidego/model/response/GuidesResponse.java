package com.example.guidego.model.response;

import com.example.guidego.model.Guide;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GuidesResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Guide> data;

    public String getMessage() { return message; }
    public List<Guide> getData() { return data; }
}

