package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;


// Trả thông tin hiện tại của variant để Order Service ghép với item trong giỏ.
public record CartInfoRepone(
        Long productVariantId,
        Long productId,
        Long shopId,
        String shopName,
        String productName,
        String imageUrl,
        boolean hasOption,
        List<VariantOption> variantOptions,
        BigDecimal price,
        int stockQuantity,
        ProductStatus status,
        Sale sale
) {

    // Mô tả một lựa chọn phân loại của variant.
    public record VariantOption(String name, String value) {
    }

    // Giá sale một đơn vị và thời điểm kết thúc; null khi không còn ưu đãi áp dụng.
    public record Sale(BigDecimal salePrice, OffsetDateTime endsAt) {
    }
}
