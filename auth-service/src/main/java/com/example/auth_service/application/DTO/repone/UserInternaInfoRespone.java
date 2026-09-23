package com.example.auth_service.application.DTO.repone;

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
