package com.example.producr_service.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegisterFlashSaleItemRequest(
        @NotNull @Positive Long variantId,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @DecimalMax(value = "100", inclusive = false) @Digits(integer = 2, fraction = 2)
        // phân trăm giảm giá
        BigDecimal discountPercent,
        @NotNull @Positive Integer registeredQuantity) {}
