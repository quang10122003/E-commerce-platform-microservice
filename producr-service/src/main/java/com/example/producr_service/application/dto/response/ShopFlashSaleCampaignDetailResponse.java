package com.example.producr_service.application.dto.response;

import java.time.OffsetDateTime;

// Tên chương trình và thời điểm đóng đăng ký tuyệt đối có offset.
public record ShopFlashSaleCampaignDetailResponse(String name, OffsetDateTime registrationDeadline) {}
