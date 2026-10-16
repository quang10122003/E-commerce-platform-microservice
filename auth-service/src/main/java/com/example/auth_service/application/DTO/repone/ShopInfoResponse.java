package com.example.auth_service.application.DTO.repone;

// shopId hiện là ID tài khoản người bán vì hệ thống chưa có bảng shop riêng.
public record ShopInfoResponse(Long shopId, String shopName, String shopLogoUrl) {
}
