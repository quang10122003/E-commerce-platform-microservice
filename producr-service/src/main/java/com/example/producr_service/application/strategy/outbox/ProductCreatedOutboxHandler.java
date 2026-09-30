package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.ProductCreatedOutboxPayload;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.domain.model.Product;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

// Đồng bộ Product đã tạo từ cơ sở dữ liệu sang Elasticsearch.
@RequiredArgsConstructor
public class ProductCreatedOutboxHandler implements OutboxEventHandler {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductSearchIndexPort productSearchIndexPort;
    private final MapJsonToObjPort mapJsonToObjPort;

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
        Long productId = payload.productId();
        String locationProduct = payload.location();

        Optional<Product> product = productRepositoryPort.findById(productId);
        // Event tạo đã hết hiệu lực nếu sản phẩm bị xóa trước khi worker xử lý.
        product.ifPresent(value -> productSearchIndexPort.index(value, locationProduct));
    }
}
