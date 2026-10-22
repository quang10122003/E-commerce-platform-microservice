package com.example.auth_service.application.DTO.repone;

import java.time.Instant;

// Địa chỉ shop chưa tách cấu trúc; response công khai không được chứa dữ liệu liên hệ.
public record PublicShopInfoResponse(Long shopId, String shopName, String location,
                                     String shopLogoUrl, String shopDescription, Instant shopCreatedAt) {}
