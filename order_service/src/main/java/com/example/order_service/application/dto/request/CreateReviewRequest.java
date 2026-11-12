package com.example.order_service.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Phần JSON "request" của form đánh giá; ảnh gửi riêng ở phần "images".
public record CreateReviewRequest(
        @NotNull(message = "Thiếu sản phẩm cần đánh giá") @Positive(message = "Sản phẩm cần đánh giá không hợp lệ") Long orderItemId,
        @NotNull(message = "Vui lòng chọn số sao") @Min(value = 1, message = "Số sao từ 1 đến 5") @Max(value = 5, message = "Số sao từ 1 đến 5") Integer rating,
        @Size(max = 2000, message = "Nội dung đánh giá tối đa 2000 ký tự") String content) {
}
