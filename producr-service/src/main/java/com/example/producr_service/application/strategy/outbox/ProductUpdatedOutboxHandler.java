package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductUpdatedOutboxHandler implements OutboxEventHandler {
    ProductRepositoryPort productRepositoryPort;
    ProductSearchIndexPort productSearchIndexPort;
    MapJsonToObjPort mapJsonToObjPort;

    @Override
    public String eventType() {
        return ProductUpdatedOutboxPayload.EVENT_TYPE;
    }

    // Đọc lại sản phẩm sau commit để chỉ mục không dùng dữ liệu cũ từ event.
    @Override
    public void handle(OutboxEventDto event) {
        ProductUpdatedOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), ProductUpdatedOutboxPayload.class);
        productRepositoryPort.findById(payload.productId())
                .ifPresent(product -> productSearchIndexPort.index(product, payload.location()));
    }
}
