package com.example.order_service.application.port.out.storage;

// File đã upload, giữ bucket và đường dẫn để xóa khi transaction rollback.
public record StoredFile(StorageBucket bucket, String objectPath) {
}
