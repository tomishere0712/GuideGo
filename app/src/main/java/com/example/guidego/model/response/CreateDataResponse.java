package com.example.guidego.model.response;

import com.google.gson.annotations.SerializedName;

public class CreateDataResponse {
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataBody data;

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public DataBody getData() { return data; }

    public static class DataBody {
        @SerializedName("id")
        private String id;
        @SerializedName("image_url")
        private String imageUrl;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("is_verified")
        private boolean isVerified;

        public String getId() { return id; }
        public String getImageUrl() { return imageUrl; }
        public String getUserId() { return userId; }
        public boolean isVerified() { return isVerified; }
    }
}

