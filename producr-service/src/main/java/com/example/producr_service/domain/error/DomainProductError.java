package  com.example.producr_service.domain.error;

import com.example.common.error.ErrorCode;

public enum DomainProductError implements ErrorCode {
    CATEGORY_NAME_REQUIRED(
            "CATEGORY_NAME_REQUIRED",
            "Ten danh muc khong duoc rong",
            400
    ),
    BRAND_NAME_REQUIRED(
            "BRAND_NAME_REQUIRED",
                    "Ten brand muc khong duoc rong",
                    400
    ),
    PRODUCT_ATTRIBUTE_NAME_REQUIRED(
            "PRODUCT_ATTRIBUTE_NAME_REQUIRED",
                    "Ten ATTRIBUTE san pham muc khong duoc rong",
                    400
    ),
    ATTRIBUTE_VALUE_NAME_REQUIRED(
            "ATTRIBUTE_VALUE_NAME_REQUIRED",
                    "Ten ATTRIBUTE_VALUE_NAME san pham muc khong duoc rong",
                    400
    ),
    MONEY_AMOUNT_NEGATIVE(
            "MONEY_AMOUNT_NEGATIVE",
                    "Money amount cannot be negative",
                    400
    );


    private final String code;
    private final String message;
    private final int httpStatusCode;

    DomainProductError(String code, String message, int httpStatusCode) {
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