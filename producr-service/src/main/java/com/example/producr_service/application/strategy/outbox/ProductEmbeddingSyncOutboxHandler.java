package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductEmbeddingSyncOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductEmbeddingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductEmbeddingSyncOutboxHandler implements OutboxEventHandler {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final ProductEmbeddingService productEmbeddingService;

    @Override
    public String eventType() {
        return ProductEmbeddingSyncOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductEmbeddingSyncOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), ProductEmbeddingSyncOutboxPayload.class);
        if (payload == null || payload.productId() == null || payload.productId() <= 0) {
            throw new IllegalArgumentException("Event đồng bộ embedding thiếu productId hợp lệ");
        }
        productEmbeddingService.syncLatest(payload.productId());
    }
}
