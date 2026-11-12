package com.example.order_service.application.dto.event;

import com.example.order_service.application.port.out.storage.StoredFile;

import java.util.List;

// Giữ cùng danh sách để listener thấy cả file được upload sau lúc đăng ký event.
public record StoredFileRollbackEvent(List<StoredFile> storedFiles) {
}
