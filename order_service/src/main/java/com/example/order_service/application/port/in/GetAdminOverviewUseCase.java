package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.AdminOverviewResponse;

public interface GetAdminOverviewUseCase {
    AdminOverviewResponse getAdminOverview();
}
