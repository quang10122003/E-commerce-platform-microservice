package com.example.producr_service.application.dto.request;

import java.time.OffsetDateTime;

// Bộ lọc chương trình DRAFT; registrationOpen null nghĩa là không lọc hạn đăng ký.
public record ShopFlashSaleCampaignFilter(
        int page,
        int size,
        OffsetDateTime from,
        OffsetDateTime to,
        Boolean registrationOpen
) {
}
