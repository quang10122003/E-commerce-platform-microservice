package com.example.order_service.cart.application.dto.repone;

import lombok.Builder;

@Builder
public record UserInternaInfoRespone(
        Long userId,
        String email,
        String fullName,
        String shopName,
        String shopPhone,
        String shopAddress,
        boolean isShopLock
) {
}
