package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import java.time.LocalDateTime;

// Dữ liệu đọc không đầy đủ items nên không được dùng để lưu aggregate.
public record FlashSaleCampaignView(
        Long id,
        String name,
        String description,
        LocalDateTime registrationDeadline,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        FlashSaleCampaignStatus status,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
