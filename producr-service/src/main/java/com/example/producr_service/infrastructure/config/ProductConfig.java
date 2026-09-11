package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.out.FileStoragePort;
import com.example.producr_service.application.registry.BucketStrategyRegistry;
import com.example.producr_service.application.service.FileService;
import com.example.producr_service.application.strategy.CategoryUploadStrategy;
import com.example.producr_service.application.strategy.ProductImageUploadStrategy;
import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProductConfig {

    // Đăng ký strategy upload ảnh category trong infrastructure layer.
    @Bean
    CategoryUploadStrategy categoryUploadStrategy() {
        return new CategoryUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product trong infrastructure layer.
    @Bean
    ProductImageUploadStrategy productImageUploadStrategy() {
        return new ProductImageUploadStrategy();
    }

    // Gom các strategy để chọn đúng logic theo loại bucket.
    @Bean
    BucketStrategyRegistry bucketStrategyRegistry(
            List<IBucketUploadStrategy> strategies
    ) {
        return new BucketStrategyRegistry(strategies);
    }

    // Đăng ký application service và cấp adapter lưu trữ cho service.
    @Bean
    FileService fileService(
            FileStoragePort fileStoragePort,
            BucketStrategyRegistry bucketStrategyRegistry
    ) {
        return new FileService(fileStoragePort, bucketStrategyRegistry);
    }
}
