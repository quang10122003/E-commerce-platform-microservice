package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.in.ProcessProductOutboxUseCase;
import com.example.producr_service.application.port.out.kafka.ProductEventPublisherPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.registry.OutboxEventHandlerRegistry;
import com.example.producr_service.application.service.StoredFileCleaner;
import com.example.producr_service.application.service.ProductOutboxStatusService;
import com.example.producr_service.application.service.ProductEmbeddingService;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.strategy.outbox.OutboxEventHandler;
import com.example.producr_service.application.strategy.outbox.ProductCreatedOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductEmbeddingSyncOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductUpdatedOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductImagesDeleteOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductvariantImagesDeleteOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductDeletedOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductvariantDeleteOutboxHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class ProductOutboxConfig {
    @Bean
    OutboxEventHandler productEmbeddingSyncOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort, ProductEmbeddingService productEmbeddingService) {
        return new ProductEmbeddingSyncOutboxHandler(mapJsonToObjPort, productEmbeddingService);
    }


    // Cung cấp service quản lý transaction khi job cập nhật trạng thái outbox.
    @Bean
    ProcessProductOutboxUseCase processProductOutboxUseCase(
            OutboxPort outboxPort,
            OutboxEventHandlerRegistry outboxEventHandlerRegistry
    ) {
        return new ProductOutboxStatusService(outboxPort, outboxEventHandlerRegistry);
    }

    // Đăng ký strategy đồng bộ Product qua các port của application.
    @Bean
    OutboxEventHandler productCreatedOutboxHandler(
            ProductRepositoryPort productRepositoryPort,
            ProductSearchIndexPort productSearchIndexPort,
            MapJsonToObjPort mapJsonToObjPort
    ) {
        return new ProductCreatedOutboxHandler(
                productRepositoryPort,
                productSearchIndexPort,
                mapJsonToObjPort
        );
    }

    // Đăng ký handler đọc lại sản phẩm đã lưu để cập nhật chỉ mục tìm kiếm.
    @Bean
    OutboxEventHandler productUpdatedOutboxHandler(
            ProductRepositoryPort productRepositoryPort,
            ProductSearchIndexPort productSearchIndexPort,
            MapJsonToObjPort mapJsonToObjPort
    ) {
        return new ProductUpdatedOutboxHandler(
                productRepositoryPort, productSearchIndexPort, mapJsonToObjPort);
    }

    // Đăng ký handler dọn ảnh sản phẩm sau khi event xóa được xử lý.
    @Bean
    OutboxEventHandler productImagesDeleteOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            StoredFileCleaner storedFileCleaner
    ) {
        return new ProductImagesDeleteOutboxHandler(mapJsonToObjPort, storedFileCleaner);
    }

    // Đăng ký handler dọn ảnh phân loại sau khi transaction xóa commit.
    @Bean
    OutboxEventHandler productvariantImagesDeleteOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            StoredFileCleaner storedFileCleaner
    ) {
        return new ProductvariantImagesDeleteOutboxHandler(mapJsonToObjPort, storedFileCleaner);
    }

    // Đăng ký handler cập nhật chỉ mục và phát event Kafka sau khi xóa phân loại.
    @Bean
    OutboxEventHandler productvariantDeleteOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            ProductSearchIndexPort productSearchIndexPort,
            ProductRepositoryPort productRepositoryPort,
            ProductEventPublisherPort productEventPublisherPort
    ) {
        return new ProductvariantDeleteOutboxHandler(
                mapJsonToObjPort, productSearchIndexPort, productRepositoryPort, productEventPublisherPort
        );
    }

    // Đăng ký handler xóa document tìm kiếm sau khi sản phẩm đã bị xóa.
    @Bean
    OutboxEventHandler productDeletedOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            ProductService productService
    ) {
        return new ProductDeletedOutboxHandler(mapJsonToObjPort, productService);
    }

    // Gom các strategy outbox để định tuyến theo eventType.
    @Bean
    OutboxEventHandlerRegistry outboxEventHandlerRegistry(
            List<OutboxEventHandler> eventHandlers
    ) {
        return new OutboxEventHandlerRegistry(eventHandlers);
    }
}
