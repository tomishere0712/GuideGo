package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Cart {
    @SerializedName("cart_id")
    private String cartId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("items")
    private List<CartItem> items;
    @SerializedName("total_amount")
    private double totalAmount;

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getItemCount() { return items != null ? items.size() : 0; }
}

