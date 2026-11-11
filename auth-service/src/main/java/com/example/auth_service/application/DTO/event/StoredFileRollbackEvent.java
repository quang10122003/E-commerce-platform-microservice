package com.example.auth_service.application.DTO.event;

import com.example.auth_service.application.port.out.storage.StoredFile;

import java.util.List;

// Giữ cùng danh sách để listener thấy cả file được upload sau lúc đăng ký event.
public record StoredFileRollbackEvent(List<StoredFile> storedFiles) {
}
