package com.example.order_service.application.port.in;

import com.example.order_service.application.port.out.storage.StoredFile;

import java.util.List;

public interface DeleteStoredFilesUseCase {
    void deleteStoredFiles(List<StoredFile> storedFiles);
}
