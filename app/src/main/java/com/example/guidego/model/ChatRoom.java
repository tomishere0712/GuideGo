package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;

public class ChatRoom {
    @SerializedName("id")
    private String id;
    @SerializedName(value = "touristId", alternate = {"tourist_id", "userId", "user_id"})
    private String touristId;
    @SerializedName(value = "guideId", alternate = {"guide_id"})
    private String guideId;
    @SerializedName(value = "guideName", alternate = {"guide_name"})
    private String guideName;
    @SerializedName(value = "touristName", alternate = {"tourist_name", "userName", "user_name"})
    private String touristName;
    @SerializedName(value = "lastMessage", alternate = {"last_message"})
    private String lastMessage;
    @SerializedName(value = "lastMessageAt", alternate = {"last_message_at"})
    private String lastMessageAt;
    @SerializedName(value = "createdAt", alternate = {"created_at"})
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

