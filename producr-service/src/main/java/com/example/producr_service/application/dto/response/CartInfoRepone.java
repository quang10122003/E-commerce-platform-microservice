package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;


// Trả thông tin hiện tại của variant để Order Service ghép với item trong giỏ.
public record CartInfoRepone(
        Long productVariantId,
        Long productId,
        String productName,
        String imageUrl,
        boolean hasOption,
        List<VariantOption> variantOptions,
        BigDecimal price,
        int stockQuantity,
        ProductStatus status
) {

    // Mô tả một lựa chọn phân loại của variant.
    public record VariantOption(String name, String value) {
    }
}
