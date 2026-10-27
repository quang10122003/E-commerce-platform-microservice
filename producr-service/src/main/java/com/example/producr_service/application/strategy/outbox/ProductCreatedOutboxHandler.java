package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.ProductCreatedOutboxPayload;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductCreatedOutboxHandler implements OutboxEventHandler {

    private final MapJsonToObjPort mapJsonToObjPort;
    private final ProductService productService;

    @Override
    public String eventType() {
        return ProductCreatedOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductCreatedOutboxPayload payload =
                mapJsonToObjPort.readValue(
                        event.payload(),
                        ProductCreatedOutboxPayload.class
                );
        productService.syncSearchIndex(payload.productId());
    }
}
