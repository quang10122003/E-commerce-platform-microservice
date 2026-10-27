package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductUpdatedOutboxHandler implements OutboxEventHandler {
    MapJsonToObjPort mapJsonToObjPort;
    ProductService productService;

    @Override
    public String eventType() {
        return ProductUpdatedOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductUpdatedOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), ProductUpdatedOutboxPayload.class);
        productService.syncSearchIndex(payload.productId());
    }
}
