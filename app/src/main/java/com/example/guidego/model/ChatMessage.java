package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("id")
    private String id;
    @SerializedName("chatId")
    private String chatId;
    @SerializedName("senderId")
    private String senderId;
    @SerializedName("senderName")
    private String senderName;
    @SerializedName("content")
    private String content;
    @SerializedName("sentAt")
    private String sentAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}

