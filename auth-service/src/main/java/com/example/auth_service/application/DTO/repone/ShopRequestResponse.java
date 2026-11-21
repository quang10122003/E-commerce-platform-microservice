package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.ShopRequest;
import com.example.auth_service.domain.model.ShopRequestStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ShopRequestResponse(Long id, String shopName, String shopDescription, String shopPhone,
                                  Integer provinceCode, String provinceName, String ward, String addressDetail,
                                  ShopRequestStatus status, String rejectReason, OffsetDateTime createdAt,
                                  OffsetDateTime reviewedAt) {
    public static ShopRequestResponse from(ShopRequest request, ZoneId zoneId) {
        return new ShopRequestResponse(request.getId(), request.getShopName(), request.getShopDescription(),
                request.getShopPhone(), request.getProvinceCode(), request.getProvinceName(), request.getWard(),
                request.getAddressDetail(), request.getStatus(), request.getRejectReason(),
                request.getCreatedAt().atZone(zoneId).toOffsetDateTime(),
                request.getReviewedAt() == null ? null : request.getReviewedAt().atZone(zoneId).toOffsetDateTime());
    }
}
