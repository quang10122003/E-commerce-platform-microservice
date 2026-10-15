package com.example.producr_service.application.port.out.Json;

public interface MapJsonToObjPort {
    // Chuyển object payload thành kiểu DTO mà handler cần xử lý.
    <T> T readValue(Object payload, Class<T> payloadType);
}
