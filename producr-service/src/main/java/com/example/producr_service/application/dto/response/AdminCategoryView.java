package com.example.producr_service.application.dto.response;

import java.time.LocalDateTime;

public record AdminCategoryView(
        Long id,
        String name,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
