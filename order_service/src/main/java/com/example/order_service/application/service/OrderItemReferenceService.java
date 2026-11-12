package com.example.order_service.application.service;

import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.ProductReviewRepoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

// Đơn hàng giữ snapshot tên, ảnh, giá; chỉ ngắt tham chiếu tới sản phẩm đã bị xóa ở product-service.
@Slf4j
@RequiredArgsConstructor
public class OrderItemReferenceService {
    private final OrderRepoPort orderRepoPort;
    private final ProductReviewRepoPort productReviewRepoPort;

    // Chạy lại khi nhận trùng event không đổi thêm dữ liệu.
    @Transactional
    public void clearDeletedProduct(Long productId) {
        int updated = orderRepoPort.clearDeletedProduct(productId);
        int reviews = productReviewRepoPort.clearDeletedProduct(productId);
        log.info("Ngắt tham chiếu sản phẩm đã xóa trong order_items và đánh giá: product_id={}, updated={}, reviews={}",
                productId, updated, reviews);
    }

    @Transactional
    public void clearDeletedVariant(Long variantId) {
        int updated = orderRepoPort.clearDeletedVariant(variantId);
        log.info("Ngắt tham chiếu biến thể đã xóa trong order_items: product_variant_id={}, updated={}", variantId, updated);
    }
}
