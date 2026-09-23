package com.example.producr_service.application.dto.response;

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
