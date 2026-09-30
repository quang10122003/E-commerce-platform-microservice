package com.example.producr_service.application.service;

import com.example.producr_service.application.port.out.storage.FileStoragePort;
import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;
import java.util.stream.Collectors;

public class StoredFileCleaner {
    private final FileStoragePort fileStoragePort;

    public StoredFileCleaner(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    // Xóa các file theo bucket để dọn storage sau rollback hoặc xử lý outbox.
    public void deleteStoredFiles(List<StoredFile> storedFiles) {
        if (storedFiles.isEmpty()) {
            return;
        }
        storedFiles.stream()
                .collect(Collectors.groupingBy(StoredFile::bucket,
                        Collectors.mapping(StoredFile::objectPath, Collectors.toList())))
                .forEach(fileStoragePort::delete);
    }
}
