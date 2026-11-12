package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.AdminAnalyticsResponse;

import java.time.OffsetDateTime;

public interface GetAdminAnalyticsUseCase {
    AdminAnalyticsResponse getAdminAnalytics(OffsetDateTime from, OffsetDateTime to);
}
