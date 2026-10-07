package com.example.producr_service.domain.model;

// Trạng thái vòng đời của chương trình Flash Sale.
public enum FlashSaleCampaignStatus {
    // Shop được đăng ký, sửa hoặc rút variant khi chưa đến registrationDeadline.
    DRAFT,
    // Đã công bố cho người mua; chỉ áp dụng giá sale trong [startsAt, endsAt).
    PUBLISHED,
    // Đã hủy; không nhận đăng ký và không áp dụng giá sale.
    CANCELLED,
    // Chương trình đã hoàn thành, khác với bị hủy.
    DONE
}
