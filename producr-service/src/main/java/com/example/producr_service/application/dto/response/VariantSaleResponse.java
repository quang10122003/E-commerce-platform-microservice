package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Giá gốc dùng giá variant hiện tại; sale null khi ưu đãi không còn áp dụng.
public record VariantSaleResponse(Long variantId, BigDecimal originalPrice, Sale sale) {
    public record Sale(BigDecimal discountPercent, BigDecimal salePrice, OffsetDateTime endsAt) {}
}
