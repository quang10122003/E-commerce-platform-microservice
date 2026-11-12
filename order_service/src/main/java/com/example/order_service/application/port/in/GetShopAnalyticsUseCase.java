package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.ShopAnalyticsResponse;

import java.time.OffsetDateTime;

public interface GetShopAnalyticsUseCase {
    ShopAnalyticsResponse getShopAnalytics(OffsetDateTime from, OffsetDateTime to);
}
