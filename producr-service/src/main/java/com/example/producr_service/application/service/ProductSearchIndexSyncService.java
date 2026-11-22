package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.response.ProductSearchIndexSyncResponse;
import com.example.producr_service.application.port.in.SyncProductSearchIndexUseCase;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ProductSearchIndexSyncService implements SyncProductSearchIndexUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductService productService;
    private final ProductEmbeddingService productEmbeddingService;
    private final ProductSearchIndexPort productSearchIndexPort;
    private final ProductEmbeddingRepositoryPort productEmbeddingRepositoryPort;

    // Không mở transaction bao cả lượt vì mỗi sản phẩm gọi Elasticsearch và Gemini; từng bước tự đọc dữ liệu mới nhất.
    @Override
    public ProductSearchIndexSyncResponse syncAllProducts() {
        // Index có thể vừa bị xóa trong lúc service đang chạy; tạo lại theo mapping trước khi ghi document.
        productSearchIndexPort.ensureIndex();
        productEmbeddingRepositoryPort.ensureIndex();
        List<Long> productIds = productRepositoryPort.findAllIds();
        log.info("Bắt đầu đồng bộ lại index tìm kiếm và embedding: total={}", productIds.size());
        List<Long> failedProductIds = new ArrayList<>();
        for (Long productId : productIds) {
            try {
                productService.syncSearchIndex(productId);
                productEmbeddingService.syncLatest(productId);
            } catch (RuntimeException exception) {
                // Một sản phẩm lỗi (ví dụ Gemini giới hạn tần suất) không được chặn các sản phẩm còn lại; id lỗi trả về để chạy lại.
                failedProductIds.add(productId);
                log.error("Đồng bộ index thất bại: product_id={}", productId, exception);
            }
        }
        int synced = productIds.size() - failedProductIds.size();
        log.info("Đã đồng bộ lại index: total={}, synced={}, failed={}", productIds.size(), synced, failedProductIds.size());
        return new ProductSearchIndexSyncResponse(productIds.size(), synced, failedProductIds);
    }
}
