package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;

public record VariantDiscountView(Long variantId, BigDecimal discountPercent,
                                  int registeredQuantity, long soldQuantity) {
}
