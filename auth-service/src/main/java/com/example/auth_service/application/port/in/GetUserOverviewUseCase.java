package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.UserOverviewResponse;

public interface GetUserOverviewUseCase {
    UserOverviewResponse getUserOverview();
}
