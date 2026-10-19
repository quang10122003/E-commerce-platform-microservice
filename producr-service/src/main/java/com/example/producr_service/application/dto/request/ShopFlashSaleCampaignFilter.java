package com.example.producr_service.application.dto.request;

import java.time.OffsetDateTime;

// registrationOpen null nghĩa là không lọc hạn đăng ký, không phải đã đóng đăng ký.
public record ShopFlashSaleCampaignFilter(
        int page,
        int size,
        OffsetDateTime from,
        OffsetDateTime to,
        Boolean registrationOpen
) {
}
