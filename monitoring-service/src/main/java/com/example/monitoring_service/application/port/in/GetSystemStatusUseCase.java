package com.example.monitoring_service.application.port.in;

import com.example.monitoring_service.application.dto.response.SystemStatusResponse;

public interface GetSystemStatusUseCase {
    SystemStatusResponse getSystemStatus();
}
