package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// sale null khi variant không có ưu đãi hiện còn áp dụng.
public record VariantSaleResponse(Long variantId, Sale sale) {
    public record Sale(BigDecimal discountPercent, BigDecimal salePrice, OffsetDateTime endsAt) {}
}
