package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.ShopRequestStatus;

import java.time.OffsetDateTime;

// Đơn mở shop kèm email, họ tên người nộp để admin đối chiếu khi duyệt.
public record AdminShopRequestResponse(Long id, Long userId, String userEmail, String userFullName, String shopName,
                                       String shopDescription, String shopPhone, Integer provinceCode,
                                       String provinceName, String ward, String addressDetail,
                                       ShopRequestStatus status, String rejectReason, Long reviewedBy,
                                       OffsetDateTime createdAt, OffsetDateTime reviewedAt) {
}
