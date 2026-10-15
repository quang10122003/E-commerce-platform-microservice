package com.example.producr_service.adapter.out.Jackson;

import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
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
    // Dùng cấu hình Jackson hiện có để chuyển payload đã đọc từ DB thành DTO cụ thể.
    @Override
    public <T> T readValue(Object payload, Class<T> payloadType) {
        try {
            return objectMapper.convertValue(payload, payloadType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Không thể chuyển payload thành " + payloadType.getSimpleName(),
                    exception
            );
        }
    }

}
