package com.example.monitoring_service.application.dto.response;

import com.example.monitoring_service.domain.model.HealthState;
import com.example.monitoring_service.domain.model.ServiceStatus;

import java.util.Map;

public record InstanceStatusResponse(
        String serviceName,
        String instanceId,
        String baseUrl,
        String registryStatus,
        ServiceStatus status,
        Map<String, HealthState> criticalComponents,
        Map<String, HealthState> optionalComponents
) {
}
