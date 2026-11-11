package com.example.auth_service.application.service;

import com.example.auth_service.application.port.in.DeleteStoredFilesUseCase;
import com.example.auth_service.application.port.out.storage.FileStoragePort;
import com.example.auth_service.application.port.out.storage.StoredFile;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DeleteStoredFilesService implements DeleteStoredFilesUseCase {
    private final FileStoragePort fileStoragePort;

    // Gom theo bucket để mỗi bucket chỉ gọi storage một lần.
    @Override
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
