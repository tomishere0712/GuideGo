package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {
    @SerializedName("chatId")
    private String chatId;
    @SerializedName("content")
    private String content;

    public SendMessageRequest(String chatId, String content) {
        this.chatId = chatId;
        this.content = content;
    }

    public String getChatId() { return chatId; }
    public String getContent() { return content; }
}

