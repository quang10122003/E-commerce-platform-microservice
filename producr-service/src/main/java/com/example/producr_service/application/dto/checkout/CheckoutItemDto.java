package com.example.producr_service.application.dto.checkout;

public record CheckoutItemDto(Long variantId, Integer quantity, Boolean sale) {
}
