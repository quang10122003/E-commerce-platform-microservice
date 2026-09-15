package com.example.producr_service.application.dto.command;

// Command dùng chung cho mọi yêu cầu upload file từ application layer.
public record UploadFileCommand(
        String fileName,
        String contentType,
        byte[] content) {
}
