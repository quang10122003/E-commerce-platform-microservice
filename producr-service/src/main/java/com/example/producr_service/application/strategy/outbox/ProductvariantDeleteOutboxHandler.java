package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductvariantDeleteOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductvariantDeleteOutboxHandler implements OutboxEventHandler{
    MapJsonToObjPort mapJsonToObjPort;
    ProductService productService;
    @Override
    public String eventType() {
        return ProductvariantDeleteOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductvariantDeleteOutboxPayload payload = mapJsonToObjPort.readValue(event.payload(),ProductvariantDeleteOutboxPayload.class);
        productService.syncAfterVariantDeletion(payload.productId(), event);
    }
}
