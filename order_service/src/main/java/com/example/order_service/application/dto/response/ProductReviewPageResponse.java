package com.example.order_service.application.dto.response;

import com.example.common.response.PageResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// Đánh giá công khai của một sản phẩm: tổng quan số sao và một trang đánh giá mới nhất.
public record ProductReviewPageResponse(Summary summary, PageResponse<Item> reviews) {

    // ratingCounts xếp từ 5 sao xuống 1 sao, luôn đủ 5 mức.
    public record Summary(BigDecimal ratingAverage, long reviewCount, List<RatingCount> ratingCounts) {
    }

    public record RatingCount(int rating, long count) {
    }

    // Tên người mua và phân loại lấy từ snapshot lúc đánh giá; avatar lấy hiện tại từ auth-service, null khi không có hoặc không gọi được.
    public record Item(Long id, String reviewerName, String reviewerAvatarUrl, int rating, String content, String variantAttributes,
                       List<String> imageUrls, OffsetDateTime createdAt) {
    }
}
