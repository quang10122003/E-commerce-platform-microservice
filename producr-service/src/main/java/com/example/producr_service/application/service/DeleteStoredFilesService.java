package com.example.producr_service.application.service;

import com.example.producr_service.application.port.in.DeleteStoredFilesUseCase;
import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;

public class DeleteStoredFilesService implements DeleteStoredFilesUseCase {
    private final StoredFileCleaner storedFileCleaner;

    public DeleteStoredFilesService(StoredFileCleaner storedFileCleaner) {
        this.storedFileCleaner = storedFileCleaner;
    }

    // Dọn các file đã upload sau khi transaction tạo sản phẩm rollback.
    @Override
    public void deleteStoredFiles(List<StoredFile> storedFiles) {
        storedFileCleaner.deleteStoredFiles(storedFiles);
    }
}
