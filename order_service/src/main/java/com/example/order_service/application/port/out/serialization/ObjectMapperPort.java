package com.example.order_service.application.port.out.serialization;

public interface ObjectMapperPort {
    <T> T readValue(String value, Class<T> payloadType);

    String writeValue(Object value);
}
