package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductDeletedOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductDeletedOutboxHandler implements OutboxEventHandler {
    MapJsonToObjPort mapJsonToObjPort;
    ProductService productService;

    @Override
    public String eventType() {
        return ProductDeletedOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductDeletedOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), ProductDeletedOutboxPayload.class
        );
        productService.cleanupDeletedProduct(payload.productId(), event);
    }
}
