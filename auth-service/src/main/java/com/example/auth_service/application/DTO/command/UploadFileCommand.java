package com.example.auth_service.application.DTO.command;

public record UploadFileCommand(String fileName, String contentType, byte[] content) {
}
