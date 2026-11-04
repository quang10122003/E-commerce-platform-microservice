package com.example.order_service.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutItemDto(
        @NotNull @Positive Long variantId,
        @NotNull @Positive Integer quantity,
        @NotNull Boolean sale) {
}
