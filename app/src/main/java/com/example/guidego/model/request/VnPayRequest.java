package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VnPayRequest {
    @SerializedName("bookingIds")
    private List<String> bookingIds;

    public VnPayRequest(List<String> bookingIds) {
        this.bookingIds = bookingIds;
    }

    public List<String> getBookingIds() { return bookingIds; }
}

