package com.example.order_service.cart.application.port.out.objettMapper;

public interface ObjectMapperPort {
    // conver string ra objcet
    <T> T readValue(String value, Class<T> payloadType);
}
