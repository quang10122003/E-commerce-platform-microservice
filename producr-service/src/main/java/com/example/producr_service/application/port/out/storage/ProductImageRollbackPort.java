package com.example.producr_service.application.port.out.storage;

import java.util.List;

public interface ProductImageRollbackPort {
    // Đăng ký dọn các ảnh đã upload nếu transaction tạo sản phẩm rollback.
    void deleteImageStore(List<StoredFile> storedFiles);
}
