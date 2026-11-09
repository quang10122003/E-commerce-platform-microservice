package com.example.monitoring_service.domain.model;
//Trạng thái thô của một health , map từ status Spring Actuator trả về.
public enum HealthState {
    UP,
    DOWN,
    OUT_OF_SERVICE,
    UNKNOWN;

    public static HealthState fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (HealthState state : values()) {
            if (state.name().equalsIgnoreCase(code)) {
                return state;
            }
        }
        return UNKNOWN;
    }
}
