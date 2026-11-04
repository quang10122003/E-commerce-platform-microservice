package com.example.order_service.domain.error;

import com.example.common.error.ErrorCode;

public enum OrderDomainError implements ErrorCode {
    INVALID_DATA("ORDER_INVALID_DATA", "Dữ liệu đơn hàng không hợp lệ", 400),
    INVALID_AMOUNT("ORDER_INVALID_AMOUNT", "Số tiền phải là số nguyên VND dương, tối đa 19 chữ số", 400),
    INVALID_QUANTITY("ORDER_INVALID_QUANTITY", "Số lượng phải lớn hơn 0", 400),
    INVALID_PRICE("ORDER_INVALID_PRICE", "Đơn giá không phù hợp với giá gốc hoặc loại giá", 400),
    TOTAL_MISMATCH("ORDER_TOTAL_MISMATCH", "Tổng tiền không khớp với số tiền đã xác nhận", 409),
    ITEMS_REQUIRED("ORDER_ITEMS_REQUIRED", "Đơn hàng phải có ít nhất một sản phẩm", 400),
    DUPLICATE_ITEM("ORDER_DUPLICATE_ITEM", "Biến thể đã tồn tại trong đơn hàng", 409),
    PRICE_TYPE_MISMATCH("ORDER_PRICE_TYPE_MISMATCH", "Loại giá của sản phẩm không khớp đơn hàng", 400),
    INVALID_STATE("ORDER_INVALID_STATE", "Trạng thái hiện tại không cho phép thao tác", 409);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    OrderDomainError(String code, String message, int httpStatusCode) {
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
