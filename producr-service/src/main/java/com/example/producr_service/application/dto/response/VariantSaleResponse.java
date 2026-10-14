package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Giá gốc là giá hiện tại của variant; sale null khi không có ưu đãi còn áp dụng.
public record VariantSaleResponse(Long variantId, BigDecimal originalPrice, Sale sale) {
    public record Sale(BigDecimal discountPercent, BigDecimal salePrice, OffsetDateTime endsAt) {}
}
