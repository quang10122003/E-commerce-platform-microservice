package  com.example.producr_service.domain.error;

import com.example.common.error.ErrorCode;

public enum DomainProductError implements ErrorCode {
    CATEGORY_NAME_REQUIRED(
            "CATEGORY_NAME_REQUIRED",
            "Tên danh mục không được để trống",
            400
    ),
    BRAND_NAME_REQUIRED(
            "BRAND_NAME_REQUIRED",
            "Tên thương hiệu không được để trống",
            400
    ),
    PRODUCT_ATTRIBUTE_NAME_REQUIRED(
            "PRODUCT_ATTRIBUTE_NAME_REQUIRED",
            "Tên thuộc tính sản phẩm không được để trống",
            400
    ),
    ATTRIBUTE_VALUE_NAME_REQUIRED(
            "ATTRIBUTE_VALUE_NAME_REQUIRED",
            "Tên giá trị thuộc tính không được để trống",
            400
    ),
    PRODUCT_CATEGORY_REQUIRED(
            "PRODUCT_CATEGORY_REQUIRED",
            "Sản phẩm phải thuộc một danh mục",
            400
    ),
    PRODUCT_NAME_REQUIRED(
            "PRODUCT_NAME_REQUIRED",
            "Tên sản phẩm không được để trống",
            400
    ),
    PRODUCT_VARIANTS_REQUIRED(
            "PRODUCT_VARIANTS_REQUIRED",
            "Sản phẩm chưa có biến thể",
            400
    ),
    SKU_REQUIRED(
            "SKU_REQUIRED",
            "SKU không được để trống",
            400
    ),
    STOCK_QUANTITY_NEGATIVE(
            "STOCK_QUANTITY_NEGATIVE",
            "Tồn kho không được âm",
            400
    ),
    STOCK_DECREASE_QUANTITY_INVALID(
            "STOCK_DECREASE_QUANTITY_INVALID",
            "Số lượng trừ kho phải lớn hơn 0",
            400
    ),
    STOCK_INSUFFICIENT(
            "STOCK_INSUFFICIENT",
            "Không đủ tồn kho để trừ",
            409
    ),
    STOCK_INCREASE_QUANTITY_INVALID(
            "STOCK_INCREASE_QUANTITY_INVALID",
            "Số lượng nhập kho phải lớn hơn 0",
            400
    ),
    IMAGE_URL_REQUIRED(
            "IMAGE_URL_REQUIRED",
            "Đường dẫn ảnh không được để trống",
            400
    ),
    VARIANT_MULTIPLE_PRIMARY_IMAGES(
            "VARIANT_MULTIPLE_PRIMARY_IMAGES",
            "Variant can only have one primary image",
            400
    ),
    MONEY_AMOUNT_NEGATIVE(
            "MONEY_AMOUNT_NEGATIVE",
            "Số tiền không được âm",
            400
    ),
    FLASH_SALE_CANNOT_CANCEL(
            "FLASH_SALE_CANNOT_CANCEL", "Không thể hủy chương trình Flash Sale đã hoàn thành", 409
    ),
    FLASH_SALE_NAME_INVALID(
            "FLASH_SALE_NAME_INVALID",
            "Tên chương trình Flash Sale phải có từ 1 đến 255 ký tự",
            400
    ),
    FLASH_SALE_TIME_INVALID(
            "FLASH_SALE_TIME_INVALID",
            "Hạn đăng ký phải trước thời điểm bắt đầu và thời điểm kết thúc",
            400
    ),
    FLASH_SALE_REGISTRATION_DEADLINE_PASSED(
            "FLASH_SALE_REGISTRATION_DEADLINE_PASSED",
            "Hạn đăng ký Flash Sale phải ở tương lai",
            400
    ),
    FLASH_SALE_START_TOO_SOON(
            "FLASH_SALE_START_TOO_SOON",
            "Thời điểm bắt đầu Flash Sale phải cách hiện tại ít nhất 3 tiếng",
            400
    ),
    FLASH_SALE_REGISTRATION_GAP_TOO_SHORT(
            "FLASH_SALE_REGISTRATION_GAP_TOO_SHORT",
            "Hạn đăng ký phải trước thời điểm bắt đầu Flash Sale ít nhất 30 phút",
            400
    ),
    FLASH_SALE_CREATOR_INVALID(
            "FLASH_SALE_CREATOR_INVALID",
            "ID người tạo chương trình Flash Sale phải lớn hơn 0",
            400
    ),
    PRODUCT_USER_REQUIRED(
            "PRODUCT_USER_REQUIRED",
            "sản phẩm phải sở hữu bởi 1 user",
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
