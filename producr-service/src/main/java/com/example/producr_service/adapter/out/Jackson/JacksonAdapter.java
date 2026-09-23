package com.example.producr_service.adapter.out.Jackson;

import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@Component
public class JacksonAdapter implements MapJsonToObjPort {
    ObjectMapper objectMapper;
    @Override
    public <T> T readValue(String payload, Class<T> payloadType) {
        try {
            return objectMapper.readValue(payload, payloadType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Không thể parse payload",
                    exception
            );
        }
    }

}
