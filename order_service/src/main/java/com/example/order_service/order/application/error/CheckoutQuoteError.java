package com.example.order_service.order.application.error;

import com.example.common.error.ErrorCode;

public enum CheckoutQuoteError implements ErrorCode {
    INVALID_QUOTE("INVALID_CART_QUOTE", "Yêu cầu tính giá không hợp lệ", 400),
    VARIANT_NOT_FOUND("PRODUCT_VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm", 404),
    SALE_CONFLICT("FLASH_SALE_SCHEDULE_CONFLICT", "Biến thể thuộc nhiều sale PUBLISHED", 409),
    PRICING_UNAVAILABLE("PRICING_UNAVAILABLE", "Tạm thời không thể tính giá sản phẩm", 503);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    CheckoutQuoteError(String code, String message, int httpStatusCode) {
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
