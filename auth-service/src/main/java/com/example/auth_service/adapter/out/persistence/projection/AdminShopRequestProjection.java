package com.example.auth_service.adapter.out.persistence.projection;

import com.example.auth_service.domain.model.ShopRequestStatus;

import java.time.LocalDateTime;

public interface AdminShopRequestProjection {
    Long getId();
    Long getUserId();
    String getUserEmail();
    String getUserFullName();
    String getShopName();
    String getShopDescription();
    String getShopPhone();
    Integer getProvinceCode();
    String getProvinceName();
    String getWard();
    String getAddressDetail();
    ShopRequestStatus getStatus();
    String getRejectReason();
    Long getReviewedBy();
    LocalDateTime getCreatedAt();
    LocalDateTime getReviewedAt();
}
