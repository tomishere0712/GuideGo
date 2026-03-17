package com.example.guidego.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateReviewRequest {
    @SerializedName("tour_id")
    private String tourId;
    @SerializedName("rating")
    private int rating;
    @SerializedName("comment")
    private String comment;

    public CreateReviewRequest(String tourId, int rating, String comment) {
        this.tourId = tourId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getTourId() { return tourId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}

