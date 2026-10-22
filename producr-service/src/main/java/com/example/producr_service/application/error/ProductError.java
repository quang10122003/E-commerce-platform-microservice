package com.example.producr_service.application.error;

import com.example.common.error.ErrorCode;

public enum ProductError implements ErrorCode  {
    PRODUCT_SEARCH_PAGE_LIMIT_EXCEEDED("PRODUCT_SEARCH_PAGE_LIMIT_EXCEEDED",
            "Trang tìm kiếm vượt giới hạn 10000 sản phẩm", 400),
    SHOP_NOT_FOUND("SHOP_NOT_FOUND", "Không tìm thấy shop", 404),
    SHOP_INFO_UNAVAILABLE("SHOP_INFO_UNAVAILABLE", "Tạm thời không thể lấy thông tin shop", 503),
    PRODUCT_ACCESS_DENIED("PRODUCT_ACCESS_DENIED", "Bạn không có quyền truy cập sản phẩm này", 403),
    FLASH_SALE_ITEM_ALREADY_EXISTS("FLASH_SALE_ITEM_ALREADY_EXISTS", "Variant đã có đăng ký trong chương trình", 409),
    FLASH_SALE_PRODUCT_INACTIVE("FLASH_SALE_PRODUCT_INACTIVE", "Sản phẩm đã bị khóa, không thể đăng ký sale", 409),
    FLASH_SALE_CAMPAIGN_NOT_FOUND(
            "FLASH_SALE_CAMPAIGN_NOT_FOUND", "Không tìm thấy chương trình Flash Sale", 404
    ),
    FLASH_SALE_CAMPAIGN_NOT_DRAFT(
            "FLASH_SALE_CAMPAIGN_NOT_DRAFT", "Chỉ được xem đăng ký của chương trình Flash Sale ở trạng thái nháp", 409
    ),
    INVALID_FLASH_SALE_CAMPAIGN_ID(
            "INVALID_FLASH_SALE_CAMPAIGN_ID", "ID chương trình Flash Sale khong hop le ", 400
    ),
    INVALID_FLASH_SALE_TIME_FILTER(
            "INVALID_FLASH_SALE_TIME_FILTER",
            "Thời điểm from không được sau thời điểm to",
            400
    ),
    INVALID_FLASH_SALE_PAGINATION(
            "INVALID_FLASH_SALE_PAGINATION",
            "Trang phải từ 1 trở lên và kích thước trang phải từ 10 đến 30",
            400
    ),
    FLASH_SALE_SCHEDULE_CONFLICT(
            "FLASH_SALE_SCHEDULE_CONFLICT",
            "Thời gian chương trình Flash Sale trùng với chương trình khác",
            409
    ),
    CATEGORY_NOT_FOUND(
            "CATEGORY_NOT_FOUND",
            "Category not found",
            404
    ),
    PRODUCT_NOT_FOUND(
            "PRODUCT_NOT_FOUND",
            "product not found",
            404
    ),
    PRODUCT_INACTIVE(
            "PRODUCT_INACTIVE",
            "Sản phẩm không ở trạng thái hoạt động, không thể tra cứu giá sale",
            409
    ),
    PRODUCT_VARIANT_NOT_FOUND(
            "PRODUCT_VARIANT_NOT_FOUND",
            "product variant not found",
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
    ),
    INVALID_VARIANT_IMAGE_MAPPING(
            "INVALID_VARIANT_IMAGE_MAPPING",
            "Variant image mapping is invalid",
            400
    ),
    INVALID_VARIANT_ATTRIBUTES(
            "INVALID_VARIANT_ATTRIBUTES",
            "Variant attributes are invalid",
            400
    ),
    INVALID_PRODUCT_UPDATE(
            "INVALID_PRODUCT_UPDATE",
            "Product update is invalid",
            400
    ),
    ATTRIBUTE_IN_USE(
            "ATTRIBUTE_IN_USE",
            "Attribute is used by a variant",
            409
    ),
    DUPLICATE_VARIANT_ATTRIBUTE_COMBINATION(
            "DUPLICATE_VARIANT_ATTRIBUTE_COMBINATION",
            "Variant attribute combination already exists",
            400
    ),
    INVALID_IMAGE_FILE(
            "INVALID_IMAGE_FILE",
            "Image file is invalid",
            400
    ),
    INVALID_PRICE_RANGE(
            "INVALID_PRICE_RANGE",
            "Price range is invalid",
            400
    ),
    SEARCH_KEYWORD_REQUIRED(
            "SEARCH_KEYWORD_REQUIRED",
            "Search keyword is required",
            400
    ),CANNOT_DELETE_LAST_VARIANT(
            "CANNOT_DELETE_LAST_VARIANT",
            "Cannot delete the last variant of a product",
            409
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
