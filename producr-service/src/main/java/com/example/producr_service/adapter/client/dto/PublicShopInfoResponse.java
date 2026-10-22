package com.example.producr_service.adapter.client.dto;

import java.time.Instant;

// Địa chỉ từ Auth chưa tách tỉnh/thành, không coi toàn bộ location là tên tỉnh.
public record PublicShopInfoResponse(Long shopId, String shopName, String location,
                                     String shopLogoUrl, String shopDescription, Instant shopCreatedAt) {}
