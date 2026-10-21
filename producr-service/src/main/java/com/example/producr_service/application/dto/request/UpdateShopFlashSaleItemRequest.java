package com.example.producr_service.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateShopFlashSaleItemRequest(
        @NotNull @Positive Integer registeredQuantity,
        // Null hoặc không truyền nghĩa là giữ nguyên % giảm giá đã đăng ký.
        @DecimalMin(value = "0", inclusive = false)
        @DecimalMax(value = "100", inclusive = false) @Digits(integer = 2, fraction = 2)
        BigDecimal discountPercent) {}
