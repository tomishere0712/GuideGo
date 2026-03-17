package com.example.guidego.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TourSearchResponse {
    @SerializedName("items")
    private List<Tour> items;
    @SerializedName("page")
    private int page;
    @SerializedName("page_size")
    private int pageSize;
    @SerializedName("total_items")
    private int totalItems;
    @SerializedName("total_pages")
    private int totalPages;

    public List<Tour> getItems() { return items; }
    public void setItems(List<Tour> items) { this.items = items; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean hasMore() { return page < totalPages; }
}

