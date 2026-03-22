package com.example.guidego.api;

import com.example.guidego.model.Booking;
import com.example.guidego.model.Cart;
import com.example.guidego.model.ChatMessage;
import com.example.guidego.model.ChatRoom;
import com.example.guidego.model.Guide;
import com.example.guidego.model.Payment;
import com.example.guidego.model.Review;
import com.example.guidego.model.Tour;
import com.example.guidego.model.TourSchedule;
import com.example.guidego.model.TourSearchResponse;
import com.example.guidego.model.User;
import com.example.guidego.model.request.AddToCartRequest;
import com.example.guidego.model.request.CreateBookingRequest;
import com.example.guidego.model.request.CreatePaymentRequest;
import com.example.guidego.model.request.CreateReviewRequest;
import com.example.guidego.model.request.LoginRequest;
import com.example.guidego.model.request.RegisterRequest;
import com.example.guidego.model.request.SendMessageRequest;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.QueryMap;
import com.example.guidego.model.request.UpdateProfileRequest;
import com.example.guidego.model.request.UpdateReviewRequest;
import com.example.guidego.model.request.VnPayRequest;
import com.example.guidego.model.response.GuidesResponse;
import com.example.guidego.model.response.LoginResponse;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.model.response.VnPayResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiService {

    // ===== AUTH =====
    @POST("auth/register")
    Call<StatusResponse> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    // ===== USER =====
    @GET("user/{id}")
    Call<User> getUserById(@Path("id") String id);

    @PUT("user/{id}")
    Call<StatusResponse> updateUser(@Path("id") String id, @Body UpdateProfileRequest body);

    // ===== TOUR =====
    @GET("tour")
    Call<List<Tour>> getAllTours();

    @GET("tour/search")
    Call<TourSearchResponse> searchTours(@QueryMap Map<String, Object> params);

    @GET("tour/{id}")
    Call<Tour> getTourById(@Path("id") String id);

    // ===== TOUR SCHEDULES (Tourist: read-only) =====
    @GET("tour-schedules/tour/{tourId}")
    Call<List<TourSchedule>> getTourSchedules(@Path("tourId") String tourId);

    @GET("tour-schedules/{id}")
    Call<TourSchedule> getTourScheduleById(@Path("id") String id);

    // ===== GUIDE =====
    @GET("guides")
    Call<GuidesResponse> getAllGuides();

    @GET("guides/{id}")
    Call<GuidesResponse> getGuideById(@Path("id") String id);

    @GET("guides/user/{userId}")
    Call<GuidesResponse> getGuideByUserId(@Path("userId") String userId);

    // ===== CART =====
    @GET("cart")
    Call<Cart> getCart();

    @POST("cart/items")
    Call<StatusResponse> addToCart(@Body AddToCartRequest body);

    @DELETE("cart/items/{itemId}")
    Call<StatusResponse> removeFromCart(@Path("itemId") String itemId);

    // ===== BOOKING =====
    @POST("bookings")
    Call<List<Booking>> createBooking(@Body CreateBookingRequest body);

    @GET("bookings")
    Call<List<Booking>> getBookings(@Query("userId") String userId);

    @GET("bookings/{id}")
    Call<Booking> getBookingById(@Path("id") String id);

    @PUT("bookings/{id}/cancel")
    Call<StatusResponse> cancelBooking(@Path("id") String id);

    // ===== PAYMENT - VNPay =====
    @POST("payments/vnpay/create-url")
    Call<VnPayResponse> createVnPayUrl(@Body VnPayRequest body);

    /** Forward VNPay return params to BE so it can verify and update booking status. */
    @GET("payments/vnpay/return")
    Call<ResponseBody> processVnPayReturn(@QueryMap Map<String, String> params);

    // ===== PAYMENT - Legacy (còn giữ cho compatibility) =====
    @POST("payments")
    Call<Payment> createPayment(@Body CreatePaymentRequest body);

    @PUT("payments/{id}/confirm")
    Call<Payment> confirmPayment(@Path("id") String id);

    @PUT("payments/{id}/fail")
    Call<Payment> failPayment(@Path("id") String id);

    @GET("payments/booking/{bookingId}")
    Call<Payment> getPaymentByBookingId(@Path("bookingId") String bookingId);

    // ===== REVIEW =====
    @GET("review")
    Call<List<Review>> getMyReviews();

    @GET("review/{id}")
    Call<Review> getReviewById(@Path("id") String id);

    @POST("review")
    Call<StatusResponse> createReview(@Body CreateReviewRequest body);

    @PUT("review/{id}")
    Call<StatusResponse> updateReview(@Path("id") String id, @Body UpdateReviewRequest body);

    @DELETE("review/{id}")
    Call<StatusResponse> deleteReview(@Path("id") String id);

    // ===== CHAT =====
    @POST("chats/get-or-create")
    Call<ChatRoom> getOrCreateChat(@Query("guideId") String guideId);

    @GET("chats/{chatId}/messages")
    Call<List<ChatMessage>> getChatMessages(@Path("chatId") String chatId);

    @POST("chats/send")
    Call<ChatMessage> sendMessage(@Body SendMessageRequest body);

    @GET("chats/my-chats")
    Call<List<ChatRoom>> getMyChats();
}
