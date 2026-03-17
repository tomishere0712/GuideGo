package com.example.guidego.model.response;

import com.google.gson.annotations.SerializedName;

public class StatusResponse {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSuccess() { return statusCode == 200; }
}

