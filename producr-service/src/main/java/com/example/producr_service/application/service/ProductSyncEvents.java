package com.example.producr_service.application.service;

import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductEmbeddingSyncOutboxPayload;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.port.out.outbox.OutboxPort;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

// Document tìm kiếm và embedding lưu bản sao tên danh mục/thương hiệu nên phải đồng bộ lại khi chúng thay đổi.
final class ProductSyncEvents {
    private ProductSyncEvents() {
    }

    static void save(OutboxPort outboxPort, Clock clock, Long productId) {
        String aggregateId = String.valueOf(productId);
        outboxPort.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT, aggregateId,
                ProductUpdatedOutboxPayload.EVENT_TYPE, new ProductUpdatedOutboxPayload(productId),
                LocalDateTime.now(clock)));
        outboxPort.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT, aggregateId,
                ProductEmbeddingSyncOutboxPayload.EVENT_TYPE, new ProductEmbeddingSyncOutboxPayload(productId),
                LocalDateTime.now(clock)));
    }
}
