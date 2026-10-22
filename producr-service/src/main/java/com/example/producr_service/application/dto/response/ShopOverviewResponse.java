package com.example.producr_service.application.dto.response;

import java.time.OffsetDateTime;

public record ShopOverviewResponse(Long shopId, String shopName, String shopLogoUrl,
                                   String shopDescription, long productCount, OffsetDateTime shopCreatedAt) {
}
