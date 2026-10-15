package com.example.order_service.cart.adapter.out.objectMapper;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Component
public class ObjectMapperAdapter implements ObjectMapperPort {
    ObjectMapper objectMapper;
    @Override
    public <T> T readValue(String value, Class<T> payloadType) {
        return objectMapper.readValue(value,payloadType);
    }
}
