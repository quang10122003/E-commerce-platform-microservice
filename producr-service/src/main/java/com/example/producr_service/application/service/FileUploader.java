package com.example.producr_service.application.service;

import com.example.producr_service.application.port.out.storage.FileStoragePort;
import com.example.producr_service.application.registry.UploadStrategyRegistry;
import com.example.producr_service.application.strategy.upload.IUploadStrategy;
import com.example.producr_service.application.strategy.upload.UploadPurpose;
import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Điều phối upload và dọn file đã tải lên khi upload thất bại.
@Slf4j
public class FileUploader {
    private final FileStoragePort fileStoragePort;
    private final UploadStrategyRegistry strategyRegistry;
    private final StoredFileCleaner storedFileCleaner;

    // Khởi tạo uploader với cổng lưu trữ, registry và bộ dọn file.
    public FileUploader(
            FileStoragePort fileStoragePort,
            UploadStrategyRegistry strategyRegistry,
            StoredFileCleaner storedFileCleaner
    ) {
        this.fileStoragePort = fileStoragePort;
        this.strategyRegistry = strategyRegistry;
        this.storedFileCleaner = storedFileCleaner;
    }

    // Upload một file theo mục đích nghiệp vụ.
    public StoredFile upload(UploadPurpose uploadPurpose, UploadFileCommand command) {
        PreparedUpload preparedUpload = prepare(uploadPurpose, command);
        String publicUrl = fileStoragePort.upload(preparedUpload);
        return toStoredFile(preparedUpload, publicUrl);
    }

    // Chuẩn bị và upload nhiều file theo cùng mục đích nghiệp vụ.
    public List<StoredFile> upload(UploadPurpose uploadPurpose, List<UploadFileCommand> commands) {
        // Chuẩn bị toàn bộ file trước để validation lỗi thì không upload file nào.
        List<PreparedUpload> preparedUploads = commands.stream()
                .map(command -> prepare(uploadPurpose, command))
                .collect(Collectors.toList());

        return uploadPreparedFiles(preparedUploads);
    }

    // Upload tuần tự và xóa các file đã lưu nếu một file phía sau thất bại.
    private List<StoredFile> uploadPreparedFiles(List<PreparedUpload> preparedUploads) {
        List<StoredFile> storedFiles = new ArrayList<>();
        try {
            for (PreparedUpload preparedUpload : preparedUploads) {
                String publicUrl = fileStoragePort.upload(preparedUpload);
                storedFiles.add(toStoredFile(preparedUpload, publicUrl));
            }
            return storedFiles;
        } catch (RuntimeException exception) {
            // lỗi thì xóa toàn bộ ảnh vừa tải lên
            try {
                storedFileCleaner.deleteStoredFiles(storedFiles);
            } catch (RuntimeException cleanupException) {
                log.error("Upload failed and cleanup also failed for {} files", storedFiles.size(), cleanupException);
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    // Đóng gói kết quả upload với vị trí object gốc trong storage.
    private StoredFile toStoredFile(PreparedUpload preparedUpload, String publicUrl) {
        return new StoredFile(
                preparedUpload.bucket(),
                preparedUpload.objectPath(),
                publicUrl
        );
    }

    // Chọn strategy và chuẩn hóa một file trước khi gửi đến storage.
    private PreparedUpload prepare(UploadPurpose uploadPurpose, UploadFileCommand command) {
        IUploadStrategy strategy = strategyRegistry.get(uploadPurpose);
        return strategy.prepare(command);
    }
}
