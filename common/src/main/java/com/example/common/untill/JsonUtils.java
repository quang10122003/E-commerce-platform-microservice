package com.example.common.untill;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    // Chuyển object thành JSON dùng chung giữa các service.
    public String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Không thể chuyển dữ liệu thành JSON", exception);
        }
    }

    // Chuyển JSON thành object để các layer phía trên không phụ thuộc kiểu lưu trữ.
    public Object fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, Object.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Không thể đọc dữ liệu JSON", exception);
        }
    }
}
