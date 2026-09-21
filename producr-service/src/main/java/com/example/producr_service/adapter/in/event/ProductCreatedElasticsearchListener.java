package com.example.producr_service.adapter.in.event;

import com.example.producr_service.application.dto.event.ProductCreatedEvent;
import com.example.producr_service.application.port.out.ProductSearchIndexPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCreatedElasticsearchListener {

    // Dùng port để listener không phụ thuộc trực tiếp Elasticsearch adapter.
    private final ProductSearchIndexPort productSearchIndexPort;

    // Đồng bộ Product sang Elasticsearch sau khi transaction MySQL commit thành công.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductCreatedEvent event) {
        try {
            productSearchIndexPort.index(event.product());
        } catch (Exception exception) {
            // Ghi log lỗi để không ảnh hưởng kết quả tạo Product đã thành công.
            log.error(
                    "Không thể đồng bộ Product id={} sang Elasticsearch",
                    event.product().getId(),
                    exception
            );
        }
    }
}
