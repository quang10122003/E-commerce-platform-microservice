package com.example.order_service.cart.application.port.out.objettMapper;

public interface ObjectMapperPort {
    <T> T readValue(String value, Class<T> payloadType);

    String writeValue(Object value);
}
