package com.example.producr_service.application.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

// Thời gian hết hạn chung và danh sách card của sale đang chạy.
public record UserFlashSaleResponse(
        OffsetDateTime endsAt,
        List<UserFlashSaleItemResponse> items
) {

    public UserFlashSaleResponse {
        items = List.copyOf(items);
    }
}
