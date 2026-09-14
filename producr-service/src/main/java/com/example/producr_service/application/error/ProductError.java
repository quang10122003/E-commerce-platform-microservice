package com.example.producr_service.application.error;

import com.example.common.error.ErrorCode;

public enum ProductError implements ErrorCode  {
    CATEGORY_NOT_FOUND(
            "CATEGORY_NOT_FOUND",
            "Category not found",
            404
    ),
    BRAND_NOT_FOUND(
            "BRAND_NOT_FOUND",
            "Brand not found",
            404
    ),
    INVALID_ATTRIBUTE_SELECTION(
            "INVALID_ATTRIBUTE_SELECTION",
            "Attribute selection is invalid",
            400
    );

    private final String code;
        private final String message;
        private final int httpStatusCode;

    ProductError(String code, String message, int httpStatusCode) {
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
