package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Chứa dữ liệu một sản phẩm trong trang quản lý của người bán.
public record SellerProductItemResponse(
        Long id,
        String name,
        String imageUrl,
        LocalDateTime createdAt,
        Long categoryId,
        String categoryName,
        DisplayStatus status,
        List<Variant> variants
) {
    // Trạng thái quản lý được xác định từ trạng thái sản phẩm và tồn kho.
    public enum DisplayStatus {
        ACTIVE,
        OUT_OF_STOCK,
        INACTIVE
    }

    // Chứa dữ liệu của một phân loại.
    public record Variant(
            Long id,
            String sku,
            BigDecimal price,
            int stockQuantity,
            List<VariantAttribute> attributes
    ) {}

    // Chứa thuộc tính được chọn cho phân loại.
    public record VariantAttribute(
            Long attributeId,
            String name,
            String value
    ) {}
}
