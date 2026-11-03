package com.example.order_service.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CheckoutQuoteRequest(
        @NotEmpty @Size(max = 50) List<@NotNull @Valid Item> items) {
    public record Item(@NotNull @Positive Long variantId, @NotNull @Positive Integer quantity) {
    }
}
