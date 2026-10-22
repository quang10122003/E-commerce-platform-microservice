package com.example.notification.adapter.out.objectMapper;

import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ObjectMapperAdapter implements ObjectMapperPort {
    private final ObjectMapper objectMapper;

    @Override
    public <T> T readValue(String value, Class<T> payloadType) {
        return objectMapper.readValue(value, payloadType);
    }
}
