package com.example.producr_service.application.dto.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record AdminCategoryResponse(
        Long id,
        String name,
        String imageUrl,
        int productCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static AdminCategoryResponse from(AdminCategoryView view, ZoneId zoneId, long productCount) {
        return new AdminCategoryResponse(
                view.id(),
                view.name(),
                view.imageUrl(),
                Math.toIntExact(productCount),
                view.createdAt() == null ? null : view.createdAt().atZone(zoneId).toOffsetDateTime(),
                view.updatedAt() == null ? null : view.updatedAt().atZone(zoneId).toOffsetDateTime()
        );
    }
}
