package com.example.order_service.domain.error;

import com.example.common.error.ErrorCode;

public enum ReviewDomainError implements ErrorCode {
    ORDER_NOT_COMPLETED("REVIEW_ORDER_NOT_COMPLETED", "Chỉ đánh giá được sản phẩm của đơn đã giao thành công", 409),
    REVIEW_PERIOD_EXPIRED("REVIEW_PERIOD_EXPIRED", "Đã quá 7 ngày kể từ khi nhận hàng, không thể đánh giá", 409),
    INVALID_RATING("REVIEW_INVALID_RATING", "Số sao đánh giá phải từ 1 đến 5", 400),
    CONTENT_TOO_LONG("REVIEW_CONTENT_TOO_LONG", "Nội dung đánh giá tối đa 2000 ký tự", 400),
    TOO_MANY_IMAGES("REVIEW_TOO_MANY_IMAGES", "Mỗi đánh giá tối đa 5 ảnh", 400),
    INVALID_IMAGE("REVIEW_INVALID_IMAGE", "Ảnh đánh giá phải là JPEG, PNG hoặc WEBP và không quá 15MB", 400),
    INVALID_DATA("REVIEW_INVALID_DATA", "Dữ liệu đánh giá không hợp lệ", 400);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    ReviewDomainError(String code, String message, int httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
