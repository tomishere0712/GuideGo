package com.example.guidego.ui.guide;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityManageSchedulesBinding;
import com.example.guidego.databinding.DialogScheduleBinding;
import com.example.guidego.model.TourSchedule;
import com.example.guidego.model.request.CreateScheduleRequest;
import com.example.guidego.model.request.UpdateScheduleRequest;
import com.example.guidego.model.response.CreateDataResponse;
import com.example.guidego.model.response.StatusResponse;
import com.example.guidego.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageSchedulesActivity extends AppCompatActivity {

    private ActivityManageSchedulesBinding binding;
    private ManageScheduleAdapter adapter;
    private String tourId;
    private String tourTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageSchedulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tourId = getIntent().getStringExtra(Constants.EXTRA_TOUR_ID);
        tourTitle = getIntent().getStringExtra("tour_title");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTourName.setText(tourTitle != null ? tourTitle : "");
        binding.fabAdd.setOnClickListener(v -> showScheduleDialog(null));

        setupRecyclerView();
        setupSwipeRefresh();
        loadSchedules();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedules();
    }

    private void setupRecyclerView() {
        adapter = new ManageScheduleAdapter(
                schedule -> showScheduleDialog(schedule),
                schedule -> confirmDelete(schedule)
        );
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadSchedules);
    }

    private void loadSchedules() {
        if (tourId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .getTourSchedules(tourId)
                .enqueue(new Callback<List<TourSchedule>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<TourSchedule>> call,
                                           @NonNull Response<List<TourSchedule>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<TourSchedule> list = response.body();
                            if (list.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerView.setVisibility(View.VISIBLE);
                                adapter.setSchedules(list);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<TourSchedule>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(ManageSchedulesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showScheduleDialog(TourSchedule existing) {
        DialogScheduleBinding db = DialogScheduleBinding.inflate(LayoutInflater.from(this));
        final String[] startDate = {existing != null ? existing.getStartDate() : ""};
        final String[] endDate = {existing != null ? existing.getEndDate() : ""};

        if (existing != null) {
            db.etStartDate.setText(existing.getStartDate());
            db.etEndDate.setText(existing.getEndDate());
            db.etSlots.setText(String.valueOf(existing.getAvailableSlots()));
        }

        db.etStartDate.setFocusable(false);
        db.etStartDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            new DatePickerDialog(this, (picker, y, m, d) -> {
                startDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                db.etStartDate.setText(startDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        db.etEndDate.setFocusable(false);
        db.etEndDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 2);
            new DatePickerDialog(this, (picker, y, m, d) -> {
                endDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                db.etEndDate.setText(endDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        String title = existing == null ? "Thêm lịch trình" : "Sửa lịch trình";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(db.getRoot())
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String sDate = startDate[0];
                    String eDate = endDate[0];
                    String slotsStr = db.etSlots.getText() != null ? db.etSlots.getText().toString().trim() : "0";
                    if (sDate.isEmpty() || eDate.isEmpty() || slotsStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int slots = Integer.parseInt(slotsStr);
                    if (existing == null) {
                        createSchedule(sDate, eDate, slots);
                    } else {
                        updateSchedule(existing.getId(), sDate, eDate, slots);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createSchedule(String startDate, String endDate, int slots) {
        CreateScheduleRequest request = new CreateScheduleRequest(tourId, startDate, endDate, slots);
        ApiClient.getInstance(this).getApiService().createTourSchedule(request)
                .enqueue(new Callback<CreateDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateDataResponse> call,
                                           @NonNull Response<CreateDataResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ManageSchedulesActivity.this,
                                    "Tạo lịch trình thành công!", Toast.LENGTH_SHORT).show();
                            loadSchedules();
                        } else {
                            Toast.makeText(ManageSchedulesActivity.this,
                                    "Không thể tạo lịch trình", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateDataResponse> call, @NonNull Throwable t) {
                        Toast.makeText(ManageSchedulesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSchedule(String id, String startDate, String endDate, int slots) {
        UpdateScheduleRequest request = new UpdateScheduleRequest(startDate, endDate, slots);
        ApiClient.getInstance(this).getApiService().updateTourSchedule(id, request)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ManageSchedulesActivity.this,
                                    "Cập nhật lịch trình thành công!", Toast.LENGTH_SHORT).show();
                            loadSchedules();
                        } else {
                            Toast.makeText(ManageSchedulesActivity.this,
                                    "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        Toast.makeText(ManageSchedulesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(TourSchedule schedule) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lịch trình")
                .setMessage("Bạn có chắc muốn xóa lịch trình " + schedule.getStartDate() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteSchedule(schedule.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteSchedule(String id) {
        ApiClient.getInstance(this).getApiService().deleteTourSchedule(id)
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusResponse> call,
                                           @NonNull Response<StatusResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ManageSchedulesActivity.this, "Đã xóa lịch trình", Toast.LENGTH_SHORT).show();
                            loadSchedules();
                        } else {
                            Toast.makeText(ManageSchedulesActivity.this,
                                    "Không thể xóa. Có thể đang có booking.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                        Toast.makeText(ManageSchedulesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


