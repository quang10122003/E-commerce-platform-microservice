package com.example.monitoring_service.application.constant;

public final class HealthGroups {

    // Phải trùng tên group khai báo trong management.endpoint.health.group của các service.
    public static final String CRITICAL = "eureka";
    public static final String OPTIONAL = "optional";

    private HealthGroups() {
    }
}
