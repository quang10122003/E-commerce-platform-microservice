package com.example.auth_service.application.port.in;

import com.example.auth_service.application.port.out.storage.StoredFile;

import java.util.List;

public interface DeleteStoredFilesUseCase {
    void deleteStoredFiles(List<StoredFile> storedFiles);
}
