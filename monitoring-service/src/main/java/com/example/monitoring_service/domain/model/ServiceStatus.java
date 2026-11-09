package com.example.monitoring_service.domain.model;
//Trạng thái tổng hợp của một service instance, được tính từ các HealthState
public enum ServiceStatus {
    UP,
    DEGRADED,
    DOWN,
    UNREACHABLE,
    MISSING
}
