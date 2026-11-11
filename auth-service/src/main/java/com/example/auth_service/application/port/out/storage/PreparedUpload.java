package com.example.auth_service.application.port.out.storage;

public record PreparedUpload(StorageBucket bucket, String objectPath, String contentType, byte[] content) {
}
