package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.out.storage.FileStoragePort;
import com.example.producr_service.application.port.in.DeleteStoredFilesUseCase;
import com.example.producr_service.application.registry.UploadStrategyRegistry;
import com.example.producr_service.application.service.FileUploader;
import com.example.producr_service.application.service.StoredFileCleaner;
import com.example.producr_service.application.service.DeleteStoredFilesService;
import com.example.producr_service.application.strategy.upload.CategoryImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.ProductImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.ProductVariantImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.IUploadStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class FileStorageConfig {

    // Đăng ký strategy upload ảnh category trong infrastructure layer.
    @Bean
    IUploadStrategy categoryImageUploadStrategy() {
        return new CategoryImageUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product trong infrastructure layer.
    @Bean
    IUploadStrategy productImageUploadStrategy() {
        return new ProductImageUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product variant đã có trong application layer.
    @Bean
    IUploadStrategy productVariantImageUploadStrategy() {
        return new ProductVariantImageUploadStrategy();
    }

    // Gom các strategy để chọn đúng logic theo loại bucket.
    @Bean
    UploadStrategyRegistry uploadStrategyRegistry(
            List<IUploadStrategy> strategies
    ) {
        return new UploadStrategyRegistry(strategies);
    }

    // Dùng chung logic dọn file cho rollback, upload lỗi và outbox.
    @Bean
    StoredFileCleaner storedFileCleaner(FileStoragePort fileStoragePort) {
        return new StoredFileCleaner(fileStoragePort);
    }

    // Cung cấp use case dọn file cho listener sau rollback.
    @Bean
    DeleteStoredFilesUseCase deleteStoredFilesUseCase(StoredFileCleaner storedFileCleaner) {
        return new DeleteStoredFilesService(storedFileCleaner);
    }

    // Cung cấp component upload file cho xử lý nội bộ.
    @Bean
    FileUploader fileUploader(
            FileStoragePort fileStoragePort,
            UploadStrategyRegistry uploadStrategyRegistry,
            StoredFileCleaner storedFileCleaner
    ) {
        return new FileUploader(fileStoragePort, uploadStrategyRegistry, storedFileCleaner);
    }
}
