package com.example.order_service.application.error;

import com.example.common.error.ErrorCode;

public enum OrderError implements ErrorCode {
    PRODUCT_UNAVAILABLE("PRODUCT_UNAVAILABLE", "Sản phẩm không còn khả dụng", 409),
    SHOP_INFO_UNAVAILABLE("SHOP_INFO_UNAVAILABLE", "Tạm thời không thể lấy thông tin shop", 503),
    CHECKOUT_PRODUCT_UNAVAILABLE("CHECKOUT_PRODUCT_UNAVAILABLE", "Sản phẩm không còn được phép mua", 409),
    CHECKOUT_SHOP_NOT_FOUND("CHECKOUT_SHOP_NOT_FOUND", "Shop không còn tồn tại", 409),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng", 404),
    SHOP_LOCKED("ORDER_SHOP_LOCKED", "Shop đang bị khóa, không thể thao tác đơn hàng", 403),
    INVALID_DATE_RANGE("ORDER_INVALID_DATE_RANGE", "Thời điểm kết thúc phải lớn hơn hoặc bằng thời điểm bắt đầu", 400),
    ANALYTICS_RANGE_TOO_LONG("ORDER_ANALYTICS_RANGE_TOO_LONG", "Khoảng thời gian thống kê tối đa 366 ngày", 400),
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
