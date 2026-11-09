package com.example.monitoring_service.application.dto;

// DTO lưu thông tin instances của các service
public record RegisteredInstance(String serviceName, String instanceId, String baseUrl, String registryStatus) {
}
