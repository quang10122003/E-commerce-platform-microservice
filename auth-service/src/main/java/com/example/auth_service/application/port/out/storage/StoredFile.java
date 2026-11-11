package com.example.auth_service.application.port.out.storage;

public record StoredFile(StorageBucket bucket, String objectPath, String publicUrl) {
}
