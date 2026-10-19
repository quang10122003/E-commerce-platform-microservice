package com.example.producr_service.application.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record UserFlashSaleResponse(
        OffsetDateTime endsAt,
        List<UserFlashSaleItemResponse> items
) {

    public UserFlashSaleResponse {
        items = List.copyOf(items);
    }
}
