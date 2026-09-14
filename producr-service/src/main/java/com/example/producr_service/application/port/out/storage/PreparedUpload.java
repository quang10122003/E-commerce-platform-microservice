package com.example.producr_service.application.port.out.storage;

// Dữ liệu upload đã chuẩn hóa để FileStoragePort gửi đến storage adapter.
public record PreparedUpload(
        StorageBucket bucket,
        String objectPath,
        String contentType,
        byte[] content
) {
}
