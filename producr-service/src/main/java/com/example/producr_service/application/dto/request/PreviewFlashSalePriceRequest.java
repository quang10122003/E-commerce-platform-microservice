package com.example.producr_service.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PreviewFlashSalePriceRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @DecimalMax(value = "100", inclusive = false) @Digits(integer = 2, fraction = 2)
        BigDecimal discountPercent) {}
