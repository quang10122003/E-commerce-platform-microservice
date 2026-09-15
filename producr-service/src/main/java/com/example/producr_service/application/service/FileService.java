package com.example.producr_service.application.service;

import com.example.producr_service.application.port.out.FileStoragePort;
import com.example.producr_service.application.registry.UploadStrategyRegistry;
import com.example.producr_service.application.strategy.UploadPurpose;
import com.example.producr_service.application.strategy.interfaces.IUploadStrategy;
import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

// Chịu trách nhiệm điều phối upload và xóa file thông qua storage port.
@Slf4j
public class FileService{
    private final FileStoragePort fileStoragePort;
    private final UploadStrategyRegistry strategyRegistry;

    // Khởi tạo service với cổng lưu trữ và registry strategy upload.
    public FileService(
            FileStoragePort fileStoragePort,
            UploadStrategyRegistry strategyRegistry
    ) {
        this.fileStoragePort = fileStoragePort;
        this.strategyRegistry = strategyRegistry;
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
        List<StoredFile> storedFiles = new java.util.ArrayList<>();
        try {
            for (PreparedUpload preparedUpload : preparedUploads) {
                String publicUrl = fileStoragePort.upload(preparedUpload);
                storedFiles.add(toStoredFile(preparedUpload, publicUrl));
            }
            return storedFiles;
        } catch (RuntimeException exception) {
            // lỗi thì xóa toàn bộ ảnh vừa tải lên
            try {
                deleteStoredFiles(storedFiles);
            } catch (RuntimeException cleanupException) {
                log.error("up ảnh lỗi và clean ảnh cx lỗi ");
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    // Xóa các file đã upload để bù trừ khi bước lưu Product không thành công.
    public void deleteStoredFiles(List<StoredFile> storedFiles) {
        if (storedFiles.isEmpty()) {
            return;
        }
        storedFiles.stream()
                .collect(Collectors.groupingBy(StoredFile::bucket,
                        Collectors.mapping(StoredFile::objectPath, Collectors.toList())))
                .forEach(fileStoragePort::delete);
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

    // Xóa ảnh trong bucket thông qua cổng lưu trữ đã cấu hình.
    public void delete(StorageBucket bucket, String objectPath) {
        fileStoragePort.delete(bucket, objectPath);
    }

    // Xóa nhiều ảnh trong cùng bucket thông qua cổng lưu trữ đã cấu hình.
    public void delete(StorageBucket bucket, List<String> objectPaths) {
        fileStoragePort.delete(bucket, objectPaths);
    }
}
