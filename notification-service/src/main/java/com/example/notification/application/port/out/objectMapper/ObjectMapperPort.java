package com.example.notification.application.port.out.objectMapper;

public interface ObjectMapperPort {
    <T> T readValue(String value, Class<T> payloadType);
}
