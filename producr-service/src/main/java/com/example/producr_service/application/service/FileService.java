package com.example.producr_service.application.service;

import com.example.producr_service.application.port.out.FileStoragePort;
import com.example.producr_service.application.registry.BucketStrategyRegistry;
import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;
import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.dto.request.UploadFileCommand;

import java.util.List;
import java.util.stream.Collectors;

// Chịu trách nhiệm điều phối upload và xóa file thông qua storage port.
public class FileService{
    private final FileStoragePort fileStoragePort;
    private final BucketStrategyRegistry strategyRegistry;

    // Khởi tạo service với cổng lưu trữ và registry strategy upload.
    public FileService(
            FileStoragePort fileStoragePort,
            BucketStrategyRegistry strategyRegistry
    ) {
        this.fileStoragePort = fileStoragePort;
        this.strategyRegistry = strategyRegistry;
    }

    // up ảnh
    public String upload(UploadFileCommand command) {
        return fileStoragePort.upload(prepare(command));
    }

    // Chuẩn bị và upload nhiều file theo cùng bucket nghiệp vụ.
    public List<String> upload(List<UploadFileCommand> commands) {
        // Chuẩn bị toàn bộ file trước để validation lỗi thì không upload file nào.
        List<PreparedUpload> preparedUploads = commands.stream()
                .map(this::prepare)
                .collect(Collectors.toList());

        return preparedUploads.stream()
                .map(fileStoragePort::upload)
                .collect(Collectors.toList());
    }

    // Chọn strategy và chuẩn hóa một file trước khi gửi đến storage.
    private PreparedUpload prepare(UploadFileCommand command) {
        IBucketUploadStrategy strategy = strategyRegistry.get(command.bucket());
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

