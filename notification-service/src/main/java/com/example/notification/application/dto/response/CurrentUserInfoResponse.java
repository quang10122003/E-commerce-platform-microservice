package com.example.notification.application.dto.response;

public record CurrentUserInfoResponse(
        Long userId,
        String email,
        String fullName,
        String shopName,
        String shopPhone,
        String shopAddress,
        boolean isShopLock
) {}
