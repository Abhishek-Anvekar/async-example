package com.healthcare.doctor_service.dto;

import java.util.List;

public class BulkResponse<T> {
    private List<T> items;

    public BulkResponse(List<T> items) {
        this.items = items;
    }

    public List<T> getItems() { return items; }
}