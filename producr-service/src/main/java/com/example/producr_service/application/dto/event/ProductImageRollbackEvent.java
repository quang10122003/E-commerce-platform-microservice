package com.example.producr_service.application.dto.event;

import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;

// Giữ cùng danh sách để listener thấy cả ảnh được upload sau lúc đăng ký event.
public record ProductImageRollbackEvent(List<StoredFile> storedFiles) {
}
