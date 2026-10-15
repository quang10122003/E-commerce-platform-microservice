package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductDeletedOutboxPayload;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.out.kafka.ProductEventPublisherPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductDeletedOutboxHandler implements OutboxEventHandler {
    MapJsonToObjPort mapJsonToObjPort;
    ProductSearchIndexPort productSearchIndexPort;
    ProductEventPublisherPort productEventPublisherPort;

    @Override
    public String eventType() {
        return ProductDeletedOutboxPayload.EVENT_TYPE;
    }

    // Xóa document tìm kiếm sau khi transaction xóa sản phẩm đã commit.
    @Override
    public void handle(OutboxEventDto event) {
        ProductDeletedOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), ProductDeletedOutboxPayload.class
        );
        productSearchIndexPort.deleteById(payload.productId());
        productEventPublisherPort.publish(event);
    }
}
