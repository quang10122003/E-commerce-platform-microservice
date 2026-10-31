package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;

public record VariantUnitPriceView(Long variantId, BigDecimal price) {
}
