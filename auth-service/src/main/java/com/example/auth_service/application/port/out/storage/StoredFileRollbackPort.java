package com.example.auth_service.application.port.out.storage;

import java.util.List;

public interface StoredFileRollbackPort {
    // Đăng ký dọn các file đã upload nếu transaction rollback.
    void deleteImageStore(List<StoredFile> storedFiles);
}
