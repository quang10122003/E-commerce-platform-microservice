package com.example.producr_service.application.port.in;

import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;

public interface DeleteStoredFilesUseCase {
    // Dọn các file đã upload khi transaction tạo sản phẩm rollback.
    void deleteStoredFiles(List<StoredFile> storedFiles);
}
