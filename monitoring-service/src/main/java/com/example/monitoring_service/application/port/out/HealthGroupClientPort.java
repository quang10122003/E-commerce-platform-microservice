package com.example.monitoring_service.application.port.out;

import com.example.monitoring_service.application.dto.HealthGroupResult;

public interface HealthGroupClientPort {
    HealthGroupResult fetchHealthGroup(String baseUrl, String groupName);
}
