package com.example.monitoring_service.application.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record SystemStatusResponse(OffsetDateTime checkedAt, List<InstanceStatusResponse> instances) {
}
