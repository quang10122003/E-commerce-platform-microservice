package com.example.producr_service.application.port.out.Json;

public interface MapJsonToObjPort {
    <T> T readValue(Object payload, Class<T> payloadType);
}
