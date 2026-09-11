package com.example.producr_service.domain.until;

// Nó chứa dữ liệu “nguyên bản” của client gửi lên để upfile 
public record UploadFileCommand(
        StorageBucket bucket,
        String fileName,
        String contentType,
        byte[] content) {
}
