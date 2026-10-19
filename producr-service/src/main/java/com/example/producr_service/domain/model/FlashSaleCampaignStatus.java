package com.example.producr_service.domain.model;

public enum FlashSaleCampaignStatus {
    // DRAFT chưa đủ cho phép đăng ký; còn phải trước registrationDeadline.
    DRAFT,
    // PUBLISHED chỉ cho áp dụng giá sale trong khoảng [startsAt, endsAt), không phải mọi thời điểm.
    PUBLISHED,
    CANCELLED,
    DONE
}
