package com.example.order_service.cart.domain.error;

import com.example.common.error.ErrorCode;

public enum CartDomainError implements ErrorCode {
    USER_ID_REQUIRED("CART_USER_ID_REQUIRED", "ID người dùng không được để trống", 400),
    PRODUCT_VARIANT_ID_REQUIRED("CART_PRODUCT_VARIANT_ID_REQUIRED", "ID biến thể sản phẩm không được để trống", 400),
    QUANTITY_INVALID("CART_QUANTITY_INVALID", "Số lượng phải lớn hơn 0", 400),
    CART_NOT_FOUND("CART_NOT_FOUND", "ko tìm thấy giở hàng của user", 404),
    ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "Sản phẩm không có trong giỏ", 404);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    CartDomainError(String code, String message, int httpStatusCode) {
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
