package com.example.producr_service.application.dto.request;

import com.example.producr_service.application.port.out.storage.StorageBucket;

// Command dùng chung cho mọi yêu cầu upload file từ application layer.
public record UploadFileCommand(
        StorageBucket bucket,
        String fileName,
        String contentType,
        byte[] content) {
}
