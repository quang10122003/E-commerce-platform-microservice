package com.example.order_service.application.dto.response;

import lombok.Builder;

@Builder
public record UserInternalInfoResponse(
        Long userId,
        String email,
        String fullName,
        String shopName,
        String shopPhone,
        String shopAddress,
        boolean isShopLock
) {
}
