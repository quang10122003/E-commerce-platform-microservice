package com.example.producr_service.application.dto.response;

import java.time.Instant;

public record ShopProfileResponse(Long shopId, String shopName, String shopLogoUrl, String shopDescription,
                                  Instant shopCreatedAt) {
}
