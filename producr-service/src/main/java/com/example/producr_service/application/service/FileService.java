package com.example.producr_service.application.service;

import com.example.producr_service.application.port.out.FileStoragePort;
import com.example.producr_service.application.registry.BucketStrategyRegistry;
import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;
import com.example.producr_service.domain.until.PreparedUpload;
import com.example.producr_service.domain.until.StorageBucket;
import com.example.producr_service.domain.until.UploadFileCommand;

import java.util.List;
import java.util.stream.Collectors;

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
        IBucketUploadStrategy strategy =
                strategyRegistry.get(command.bucket());

        PreparedUpload preparedUpload =
                strategy.prepare(command);

        return fileStoragePort.upload(preparedUpload);
    }

    // Chuẩn bị và upload nhiều file theo cùng bucket nghiệp vụ.
    public List<String> upload(List<UploadFileCommand> commands) {
        return commands.stream()
                .map(this::upload)
                .collect(Collectors.toList());
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
