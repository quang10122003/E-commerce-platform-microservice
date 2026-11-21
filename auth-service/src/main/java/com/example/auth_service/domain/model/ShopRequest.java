package com.example.auth_service.domain.model;

import com.example.auth_service.domain.error.DomainAuthError;
import com.example.common.exception.BusinessException;

import java.time.LocalDateTime;

public class ShopRequest {
    private final Long id;
    private final Long userId;
    private final String shopName;
    private final String shopDescription;
    private final String shopPhone;
    private final Integer provinceCode;
    private final String provinceName;
    private final String ward;
    private final String addressDetail;
    private ShopRequestStatus status;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShopRequest(Long id, Long userId, String shopName, String shopDescription, String shopPhone,
                       Integer provinceCode, String provinceName, String ward, String addressDetail,
                       ShopRequestStatus status, String rejectReason, Long reviewedBy, LocalDateTime reviewedAt,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.shopName = shopName;
        this.shopDescription = shopDescription;
        this.shopPhone = shopPhone;
        this.provinceCode = provinceCode;
        this.provinceName = provinceName;
        this.ward = ward;
        this.addressDetail = addressDetail;
        this.status = status;
        this.rejectReason = rejectReason;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Mô tả rỗng lưu null để không phân biệt "không nhập" với "nhập khoảng trắng".
    public static ShopRequest create(Long userId, String shopName, String shopDescription, String shopPhone,
                                     Integer provinceCode, String provinceName, String ward, String addressDetail,
                                     LocalDateTime now) {
        String description = shopDescription == null || shopDescription.isBlank() ? null : shopDescription.trim();
        return new ShopRequest(null, userId, shopName.trim(), description, shopPhone.trim(), provinceCode,
                provinceName.trim(), ward.trim(), addressDetail.trim(), ShopRequestStatus.PENDING, null, null, null,
                now, now);
    }

    public void cancel(LocalDateTime now) {
        assertPending();
        status = ShopRequestStatus.CANCELLED;
        updatedAt = now;
    }

    public void approve(Long adminId, LocalDateTime now) {
        assertPending();
        status = ShopRequestStatus.APPROVED;
        markReviewed(adminId, now);
    }

    public void reject(Long adminId, String reason, LocalDateTime now) {
        assertPending();
        status = ShopRequestStatus.REJECTED;
        rejectReason = reason.trim();
        markReviewed(adminId, now);
    }

    // Ghép theo dạng "chi tiết, xã, tỉnh" vì product-service lấy tên tỉnh sau dấu phẩy cuối.
    public String fullAddress() {
        return addressDetail + ", " + ward + ", " + provinceName;
    }

    private void assertPending() {
        if (status != ShopRequestStatus.PENDING) {
            throw new BusinessException(DomainAuthError.SHOP_REQUEST_NOT_PENDING);
        }
    }

    private void markReviewed(Long adminId, LocalDateTime now) {
        reviewedBy = adminId;
        reviewedAt = now;
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public Integer getProvinceCode() {
        return provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public String getWard() {
        return ward;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public ShopRequestStatus getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
