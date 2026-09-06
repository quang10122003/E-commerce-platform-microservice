package com.example.notification.application.dto.event;

import java.util.Arrays;

public enum EventType {

    USER_REGISTERED("UserRegistered");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventType fromValue(String value) {
        return Arrays.stream(values())
                .filter(eventType -> eventType.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không hỗ trợ event type: " + value));
    }
}
