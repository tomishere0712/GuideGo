package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentRequest {
    @SerializedName("BookingId")
    private String bookingId;
    @SerializedName("PaymentMethod")
    private String paymentMethod;

    public CreatePaymentRequest(String bookingId, String paymentMethod) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
    }

    public String getBookingId() { return bookingId; }
    public String getPaymentMethod() { return paymentMethod; }
}

