package com.example.order_service.order.application.error;

import com.example.common.error.ErrorCode;

public enum OrderError implements ErrorCode {
    CHECKOUT_PRODUCT_UNAVAILABLE("CHECKOUT_PRODUCT_UNAVAILABLE", "Sản phẩm không còn được phép mua", 409),
    CHECKOUT_SHOP_NOT_FOUND("CHECKOUT_SHOP_NOT_FOUND", "Shop không còn tồn tại", 409),
    CHECKOUT_IDEMPOTENCY_CONFLICT("CHECKOUT_IDEMPOTENCY_CONFLICT", "Yêu cầu checkout đã được tiếp nhận", 409);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    OrderError(String code, String message, int httpStatusCode) {
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
