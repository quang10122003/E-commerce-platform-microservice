package com.example.producr_service.application.port.out.Json;

public interface MapJsonToObjPort {
    // đọc json thành obj
    <T> T readValue(String payload, Class<T> payloadType);
}
