package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("id")
    private String id;
    @SerializedName("bookingId")
    private String bookingId;
    @SerializedName("amount")
    private double amount;
    @SerializedName("paymentMethod")
    private String paymentMethod;
    @SerializedName("status")
    private String status;
    @SerializedName("paidAt")
    private String paidAt;

    public static final String METHOD_CASH = "Cash";
    public static final String METHOD_BANK_TRANSFER = "BankTransfer";
    public static final String METHOD_CREDIT_CARD = "CreditCard";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_FAILED = "Failed";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
}

