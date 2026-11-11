package com.example.auth_service.adapter.out.serialization;

import com.example.auth_service.application.port.out.PayloadMapperPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PayloadMapperAdapter implements PayloadMapperPort {
    private final ObjectMapper objectMapper;

    @Override
    public <T> T convert(Object payload, Class<T> type) {
        return objectMapper.convertValue(payload, type);
    }
}
