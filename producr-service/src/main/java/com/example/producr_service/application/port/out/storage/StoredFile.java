package com.example.producr_service.application.port.out.storage;

// Thông tin file đã lưu để gán URL và hoàn tác upload khi cần.
public record StoredFile(
        StorageBucket bucket,
        String objectPath,
        String publicUrl
) {
}
