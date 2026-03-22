package com.example.guidego.model.response;

import com.example.guidego.model.Location;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LocationResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Location> data;

    public String getMessage() { return message; }
    public List<Location> getData() { return data; }
}

