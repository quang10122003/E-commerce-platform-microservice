package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.embedding.ProductEmbeddingContent;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.port.out.embedding.TextEmbeddingPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingService {
    private final ProductEmbeddingRepositoryPort productEmbeddingRepositoryPort;
    private final TextEmbeddingPort textEmbeddingPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductEmbeddingContentBuilderService productEmbeddingContentBuilderService;

    public void syncLatest(Long productId) {
        Optional<Product> product = productRepositoryPort.findById(productId);
        if (product.isEmpty()) {
            log.info("Bỏ qua đồng bộ embedding vì sản phẩm đã bị xóa productId={}", productId);
            return;
        }
        // Đọc trạng thái hiện tại để retry không dùng nội dung cũ chụp trong event.
        sync(productEmbeddingContentBuilderService.build(product.get()));
    }

    // lấy data và check điệu liện hash để call api tạo vector
    public void sync(ProductEmbeddingContent content) {
        boolean unchanged = productEmbeddingRepositoryPort
                .findContentHashByProductId(content.productId())
                .filter(content.contentHash()::equals)
                .isPresent();

        if (unchanged) {
            log.info("Bỏ qua tạo embedding vì nội dung không đổi productId={}", content.productId());
            return;
        }

        List<Float> vector = textEmbeddingPort.embed(content.text());
        ProductEmbeddingData embedding = new ProductEmbeddingData(
                content.productId(), vector, content.contentHash());
        productEmbeddingRepositoryPort.save(embedding);

        log.info("Đã lưu embedding sản phẩm productId={}, contentHash={}",
                content.productId(), content.contentHash());
    }
}
