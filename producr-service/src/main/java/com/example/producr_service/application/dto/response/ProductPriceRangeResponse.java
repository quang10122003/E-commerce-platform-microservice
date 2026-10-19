package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;

public record ProductPriceRangeResponse(Long productId, BigDecimal minPrice, BigDecimal maxPrice) {
}
