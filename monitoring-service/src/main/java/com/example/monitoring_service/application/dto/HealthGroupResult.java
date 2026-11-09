package com.example.monitoring_service.application.dto;

import java.util.Map;

// heal của 1 groups actuator spring của 1 install đã cấu hình
public record HealthGroupResult(HealthGroupOutcome outcome, String status, Map<String, String> components) {

    public HealthGroupResult {
        components = components == null ? Map.of() : Map.copyOf(components);
    }

    /** Gọi thành công: giữ status tổng của group và status từng component. */
    public static HealthGroupResult available(String status, Map<String, String> components) {
        return new HealthGroupResult(HealthGroupOutcome.AVAILABLE, status, components);
    }

    /** Instance trả 404: group chưa được khai báo, không có dữ liệu health. */
    public static HealthGroupResult notConfigured() {
        return new HealthGroupResult(HealthGroupOutcome.NOT_CONFIGURED, null, Map.of());
    }

    /** Không gọi được endpoint (timeout, lỗi kết nối, mã HTTP lạ), không có dữ liệu health. */
    public static HealthGroupResult unreachable() {
        return new HealthGroupResult(HealthGroupOutcome.UNREACHABLE, null, Map.of());
    }
}
