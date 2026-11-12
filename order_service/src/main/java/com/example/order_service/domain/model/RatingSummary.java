package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.order_service.domain.error.ReviewDomainError;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Tổng số và tổng điểm đánh giá của một sản phẩm; lưu tổng điểm thay vì trung bình để cộng thêm không bị sai số.
public record RatingSummary(long reviewCount, long ratingSum) {
    // Chặn số liệu vô lý: số đánh giá âm hoặc tổng điểm nằm ngoài khoảng 1–5 sao mỗi đánh giá.
    public RatingSummary {
        if (reviewCount < 0 || ratingSum < reviewCount || ratingSum > reviewCount * ProductReview.MAX_RATING) {
            throw new BusinessException(ReviewDomainError.INVALID_DATA);
        }
    }

    // Trả về tổng mới sau khi cộng thêm một đánh giá có số sao rating.
    public RatingSummary add(int rating) {
        return new RatingSummary(reviewCount + 1, ratingSum + rating);
    }

    // Tính điểm trung bình = tổng điểm / số đánh giá, làm tròn 2 chữ số khớp cột DECIMAL(3,2); chưa có đánh giá thì là 5 sao.
    public BigDecimal average() {
        if (reviewCount == 0) {
            return BigDecimal.valueOf(ProductReview.MAX_RATING).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(ratingSum).divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);
    }
}
