package com.example.guidego.ui.tour;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityMyTourRequestsBinding;
import com.example.guidego.databinding.DialogCreateTourRequestBinding;
import com.example.guidego.model.Booking;
import com.example.guidego.model.Cart;
import com.example.guidego.model.Location;
import com.example.guidego.model.SuitableGuide;
import com.example.guidego.model.Tour;
import com.example.guidego.model.TourSchedule;
import com.example.guidego.model.request.AddToCartRequest;
import com.example.guidego.model.request.AssignGuideRequest;
import com.example.guidego.model.request.CreateBookingRequest;
import com.example.guidego.model.request.CreateTourRequestRequest;
import com.example.guidego.model.response.CreateDataResponse;
import com.example.guidego.model.response.LocationResponse;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.ui.payment.PaymentActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTourRequestsActivity extends AppCompatActivity {

    private ActivityMyTourRequestsBinding binding;
    private TourRequestAdapter adapter;
    private List<Location> locationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyTourRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new TourRequestAdapter(this::showAssignGuideDialog);
        adapter.setOnBookNowClick(this::addToCartAndCheckout);
        adapter.setOnPayBookingClick(this::payExistingBooking);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabCreate.setOnClickListener(v -> showCreateRequestDialog());
        binding.swipeRefresh.setOnRefreshListener(this::loadRequests);

        loadRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private void loadRequests() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .getMyTourRequests()
                .enqueue(new Callback<List<Tour>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Tour>> call,
                                           @NonNull Response<List<Tour>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Tour> data = response.body();
                            if (data.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                adapter.setItems(data);
                                // Also load bookings to mark already-paid tours
                                loadPaidTourIds();
                            }
                        } else {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(MyTourRequestsActivity.this,
                                "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Loads bookings for current user, builds scheduleId→Booking map, updates adapter. */
    private void loadPaidTourIds() {
        String userId = new TokenManager(this).getUserId();
        if (userId.isEmpty()) return;
        ApiClient.getInstance(this).getApiService()
                .getBookings(userId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call,
                                           @NonNull Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Build scheduleId → best non-cancelled booking
                            // Priority: Confirmed/Completed > Pending
                            Map<String, Booking> scheduleMap = new HashMap<>();
                            for (Booking booking : response.body()) {
                                String s = booking.getStatus();
                                if (Booking.STATUS_CANCELLED.equals(s)) continue;
                                String scheduleId = booking.getScheduleId();
                                if (scheduleId == null) continue;
                                Booking existing = scheduleMap.get(scheduleId);
                                if (existing == null) {
                                    scheduleMap.put(scheduleId, booking);
                                } else if ((Booking.STATUS_CONFIRMED.equals(s)
                                        || Booking.STATUS_COMPLETED.equals(s))
                                        && Booking.STATUS_PENDING.equals(existing.getStatus())) {
                                    // Prefer confirmed over pending
                                    scheduleMap.put(scheduleId, booking);
                                }
                            }
                            adapter.setScheduleBookingMap(scheduleMap);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                        // silently ignore — buttons stay in default state
                    }
                });
    }

    /**
     * Tourist taps "Thanh toán ngay" — there's already a Pending booking.
     * Navigate directly to PaymentActivity without creating a new booking.
     */
    private void payExistingBooking(Booking booking) {
        List<Booking> bookings = Collections.singletonList(booking);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(Constants.EXTRA_BOOKING_IDS, new Gson().toJson(bookings));
        intent.putExtra(Constants.EXTRA_TOTAL_AMOUNT, booking.getTotalPrice());
        startActivity(intent);
    }

    // ── Step 5-7: addToCart → getCart → createBooking → PaymentActivity ──────────

    /**
     * Called when tourist taps "Đặt ngay" on an Accepted custom tour.
     * Chain: POST /api/cart/items → GET /api/cart → POST /api/bookings → PaymentActivity
     */
    private void addToCartAndCheckout(Tour tour) {
        if (tour.getSchedules() == null || tour.getSchedules().isEmpty()) {
            Toast.makeText(this, "Tour chưa có lịch trình", Toast.LENGTH_SHORT).show();
            return;
        }
        TourSchedule schedule = tour.getSchedules().get(0);
        // For a custom tour, max_people = the group size the tourist originally requested.
        // Use max_people directly; cap at available_slots if slots are partially taken.
        int peopleCount = tour.getMaxPeople() > 0 ? tour.getMaxPeople() : 1;
        if (schedule.getAvailableSlots() > 0 && peopleCount > schedule.getAvailableSlots()) {
            peopleCount = schedule.getAvailableSlots();
        }
        if (peopleCount <= 0) peopleCount = 1;

        binding.progressBar.setVisibility(View.VISIBLE);

        // Step 5: add to cart
        AddToCartRequest addReq = new AddToCartRequest(tour.getId(), schedule.getId(), peopleCount);
        ApiClient.getInstance(this).getApiService()
                .addToCart(addReq)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        // Proceed even on 4xx (item might already be in cart).
                        // fetchCartAndBook will show "Giỏ hàng trống" if cart is truly empty.
                        fetchCartAndBook();
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyTourRequestsActivity.this,
                                "Không thể thêm vào giỏ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCartAndBook() {
        ApiClient.getInstance(this).getApiService()
                .getCart()
                .enqueue(new Callback<Cart>() {
                    @Override
                    public void onResponse(@NonNull Call<Cart> call,
                                           @NonNull Response<Cart> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Không lấy được giỏ hàng", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Cart cart = response.body();
                        if (cart.getCartId() == null || cart.getCartId().isEmpty()) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Step 6b: create booking from cart
                        createBookingAndPay(cart);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Cart> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyTourRequestsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createBookingAndPay(Cart cart) {
        ApiClient.getInstance(this).getApiService()
                .createBooking(new CreateBookingRequest(cart.getCartId()))
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Booking>> call,
                                           @NonNull Response<List<Booking>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().isEmpty()) {
                            String msg = "Không thể tạo đơn đặt";
                            try {
                                if (response.errorBody() != null) {
                                    String json = response.errorBody().string();
                                    com.google.gson.JsonObject obj =
                                            new Gson().fromJson(json, com.google.gson.JsonObject.class);
                                    if (obj.has("message")) msg = obj.get("message").getAsString();
                                }
                            } catch (Exception ignored) {}
                            Toast.makeText(MyTourRequestsActivity.this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }
                        List<Booking> bookings = response.body();
                        double total = cart.getTotalAmount();

                        // Step 7: navigate to PaymentActivity
                        Intent intent = new Intent(MyTourRequestsActivity.this, PaymentActivity.class);
                        intent.putExtra(Constants.EXTRA_BOOKING_IDS, new Gson().toJson(bookings));
                        intent.putExtra(Constants.EXTRA_TOTAL_AMOUNT, total);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyTourRequestsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Create request dialog ────────────────────────────────────────────────────

    private void showCreateRequestDialog() {
        DialogCreateTourRequestBinding db = DialogCreateTourRequestBinding.inflate(
                LayoutInflater.from(this));

        final String[] startDate = {""};
        final String[] endDate = {""};
        // Type-safe holder — shared between the "Find guides" callback and the submit button
        final List<SuitableGuide>[] guidesHolder = new List[]{new ArrayList<>()};

        db.etStartDate.setOnClickListener(v -> pickDate(db.etStartDate, startDate, 1));
        db.etEndDate.setOnClickListener(v -> pickDate(db.etEndDate, endDate, 2));

        // Load locations for spinner
        loadLocationsIntoSpinner(db);

        // "Find suitable guides" button
        db.btnFindGuides.setOnClickListener(v -> {
            Object selected = db.spinnerLocation.getSelectedItem();
            if (!(selected instanceof Location)) {
                Toast.makeText(this, "Chọn địa điểm trước", Toast.LENGTH_SHORT).show();
                return;
            }
            loadSuitableGuides(db, ((Location) selected).getId(), guidesHolder);
        });

        new AlertDialog.Builder(this)
                .setTitle("Tạo yêu cầu tour riêng")
                .setView(db.getRoot())
                .setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
                    String title = db.etTitle.getText() != null ? db.etTitle.getText().toString().trim() : "";
                    String desc = db.etDescription.getText() != null ? db.etDescription.getText().toString().trim() : "";
                    String peopleStr = db.etPeople.getText() != null ? db.etPeople.getText().toString().trim() : "";
                    String budgetStr = db.etBudget.getText() != null ? db.etBudget.getText().toString().trim() : "";

                    if (title.isEmpty() || startDate[0].isEmpty() || endDate[0].isEmpty()
                            || peopleStr.isEmpty() || budgetStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Object selLoc = db.spinnerLocation.getSelectedItem();
                    if (!(selLoc instanceof Location)) {
                        Toast.makeText(this, "Chọn địa điểm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Location loc = (Location) selLoc;

                    int people;
                    double budget;
                    try {
                        people = Integer.parseInt(peopleStr);
                        budget = Double.parseDouble(budgetStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Số người / ngân sách không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CreateTourRequestRequest req = new CreateTourRequestRequest(
                            title, desc, loc.getId(), startDate[0], endDate[0], people, budget);

                    // Attach preferred guide if selected — use typed holder, NOT getTag()
                    if (db.spinnerGuide.getVisibility() == View.VISIBLE) {
                        int pos = db.spinnerGuide.getSelectedItemPosition();
                        // pos 0 = "— Không chọn HDV —", pos 1..n = actual guides
                        if (pos > 0) {
                            List<SuitableGuide> guides = guidesHolder[0];
                            if (guides != null && pos - 1 < guides.size()) {
                                req.setPreferredGuideId(guides.get(pos - 1).getGuideId());
                            }
                        }
                    }

                    submitRequest(req);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void pickDate(com.google.android.material.textfield.TextInputEditText et,
                          String[] target, int offsetDays) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, offsetDays);
        new DatePickerDialog(this, (picker, y, m, d) -> {
            String date = String.format("%04d-%02d-%02d", y, m + 1, d);
            target[0] = date;
            et.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadLocationsIntoSpinner(DialogCreateTourRequestBinding db) {
        ApiClient.getInstance(this).getApiService()
                .getAllLocations()
                .enqueue(new Callback<LocationResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LocationResponse> call,
                                           @NonNull Response<LocationResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            locationList = response.body().getData();
                            ArrayAdapter<Location> adapter = new ArrayAdapter<>(
                                    MyTourRequestsActivity.this,
                                    android.R.layout.simple_spinner_item, locationList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            db.spinnerLocation.setAdapter(adapter);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<LocationResponse> call, @NonNull Throwable t) {}
                });
    }

    private void loadSuitableGuides(DialogCreateTourRequestBinding db, String locationId,
                                     List<SuitableGuide>[] guidesHolder) {
        db.btnFindGuides.setEnabled(false);
        db.btnFindGuides.setText("Đang tìm...");
        ApiClient.getInstance(this).getApiService()
                .getSuitableGuides(locationId, null, true, 20)
                .enqueue(new Callback<List<SuitableGuide>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<SuitableGuide>> call,
                                           @NonNull Response<List<SuitableGuide>> response) {
                        db.btnFindGuides.setEnabled(true);
                        db.btnFindGuides.setText("🔍  Tìm hướng dẫn viên phù hợp");
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            List<SuitableGuide> guides = response.body();
                            // Store in typed holder — no setTag/getTag needed
                            guidesHolder[0] = guides;

                            // Build display list with "Không chọn" at top
                            List<String> names = new ArrayList<>();
                            names.add("— Không chọn HDV —");
                            for (SuitableGuide g : guides) {
                                names.add(g.getGuideName() + " ★" +
                                        String.format("%.1f", g.getRating()) +
                                        " · " + g.getExperienceYears() + " năm KN");
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    MyTourRequestsActivity.this,
                                    android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            db.spinnerGuide.setAdapter(adapter);
                            db.tvGuideLabel.setVisibility(View.VISIBLE);
                            db.spinnerGuide.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Không tìm thấy HDV phù hợp", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<SuitableGuide>> call, @NonNull Throwable t) {
                        db.btnFindGuides.setEnabled(true);
                        db.btnFindGuides.setText("🔍  Tìm hướng dẫn viên phù hợp");
                        Toast.makeText(MyTourRequestsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitRequest(CreateTourRequestRequest req) {
        ApiClient.getInstance(this).getApiService()
                .createTourRequest(req)
                .enqueue(new Callback<CreateDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateDataResponse> call,
                                           @NonNull Response<CreateDataResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Đã gửi yêu cầu tour!", Toast.LENGTH_SHORT).show();
                            loadRequests();
                        } else {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Không thể gửi yêu cầu", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CreateDataResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MyTourRequestsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAssignGuideDialog(Tour tour) {
        if (locationList.isEmpty()) {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }
        // Find location id of tour
        String locId = tour.getLocationId();
        if (locId == null || locId.isEmpty()) {
            Toast.makeText(this, "Tour chưa có địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getInstance(this).getApiService()
                .getSuitableGuides(locId, null, true, 20)
                .enqueue(new Callback<List<SuitableGuide>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<SuitableGuide>> call,
                                           @NonNull Response<List<SuitableGuide>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            showPickGuideDialog(tour, response.body());
                        } else {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Không có HDV phù hợp", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<SuitableGuide>> call, @NonNull Throwable t) {
                        Toast.makeText(MyTourRequestsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPickGuideDialog(Tour tour, List<SuitableGuide> guides) {
        String[] names = new String[guides.size()];
        for (int i = 0; i < guides.size(); i++) {
            SuitableGuide g = guides.get(i);
            names[i] = g.getGuideName() + " ★" + String.format("%.1f", g.getRating())
                    + " · " + g.getExperienceYears() + " năm KN";
        }
        final int[] selected = {0};
        new AlertDialog.Builder(this)
                .setTitle("Chọn hướng dẫn viên mới")
                .setSingleChoiceItems(names, 0, (d, which) -> selected[0] = which)
                .setPositiveButton("Xác nhận", (d, w) -> {
                    String guideId = guides.get(selected[0]).getGuideId();
                    assignGuide(tour.getId(), guideId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void assignGuide(String tourId, String guideId) {
        ApiClient.getInstance(this).getApiService()
                .assignGuide(tourId, new AssignGuideRequest(guideId))
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Đã cập nhật hướng dẫn viên!", Toast.LENGTH_SHORT).show();
                            loadRequests();
                        } else {
                            Toast.makeText(MyTourRequestsActivity.this,
                                    "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MyTourRequestsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

