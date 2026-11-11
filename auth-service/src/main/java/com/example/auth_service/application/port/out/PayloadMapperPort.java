package com.example.auth_service.application.port.out;

// Payload outbox đọc lại từ DB là cấu trúc JSON chung, cần chuyển về DTO có kiểu.
public interface PayloadMapperPort {
    <T> T convert(Object payload, Class<T> type);
}
