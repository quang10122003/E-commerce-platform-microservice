package com.example.monitoring_service.adapter.out.health.dto;

import java.util.Map;

public record ActuatorHealthResponse(String status, Map<String, ActuatorComponentResponse> components) {
}
