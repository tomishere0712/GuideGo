package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateBookingRequest {
    @SerializedName("CartId")
    private String cartId;

    public CreateBookingRequest(String cartId) {
        this.cartId = cartId;
    }

    public String getCartId() { return cartId; }
}

