package com.example.auth_service.application.DTO.repone;

import java.time.Instant;

public record ShopAddressView(Long shopId, String shopName, String shopAddress,
                              String shopLogoUrl, String shopDescription, Instant shopCreatedAt) {}
