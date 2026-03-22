package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;

public class ChatRoom {
    @SerializedName("id")
    private String id;
    @SerializedName("touristId")
    private String touristId;
    @SerializedName("guideId")
    private String guideId;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("touristName")
    private String touristName;
    @SerializedName("lastMessage")
    private String lastMessage;
    @SerializedName("lastMessageAt")
    private String lastMessageAt;
    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTouristId() { return touristId; }
    public void setTouristId(String touristId) { this.touristId = touristId; }
    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }
    public String getGuideName() { return guideName; }
    public void setGuideName(String guideName) { this.guideName = guideName; }
    public String getTouristName() { return touristName; }
    public void setTouristName(String touristName) { this.touristName = touristName; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(String lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

