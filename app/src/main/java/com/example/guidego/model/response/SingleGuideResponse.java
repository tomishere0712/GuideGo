package com.example.guidego.model.response;

import com.example.guidego.model.Guide;
import com.google.gson.annotations.SerializedName;

/**
 * Response wrapper for single-guide endpoints:
 *   GET /api/guides/{id}
 *   GET /api/guides/user/{userId}
 * Both return {"message":"...","data":{...}} where data is ONE Guide object (not a list).
 */
public class SingleGuideResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Guide data;

    public String getMessage() { return message; }
    public Guide getData() { return data; }
}

