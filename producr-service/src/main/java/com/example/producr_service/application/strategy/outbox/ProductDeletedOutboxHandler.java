package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductDeletedOutboxPayload;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;

public class ProductDeletedOutboxHandler implements OutboxEventHandler {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final ProductSearchIndexPort productSearchIndexPort;

    public ProductDeletedOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            ProductSearchIndexPort productSearchIndexPort
    ) {
        this.mapJsonToObjPort = mapJsonToObjPort;
        this.productSearchIndexPort = productSearchIndexPort;
    }

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
    }
}
