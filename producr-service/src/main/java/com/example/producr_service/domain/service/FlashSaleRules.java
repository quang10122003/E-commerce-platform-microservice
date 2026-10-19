package com.example.producr_service.domain.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FlashSaleRules {
    private FlashSaleRules() {}

    // Giá VND làm tròn xuống đến đơn vị nguyên để đăng ký và mua dùng cùng công thức.
    public static BigDecimal calculateSalePrice(BigDecimal originalPrice, BigDecimal discountPercent) {
        if (originalPrice == null || originalPrice.signum() <= 0
                || discountPercent == null || discountPercent.signum() <= 0
                || discountPercent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new BusinessException(DomainProductError.FLASH_SALE_PRICE_INVALID);
        }
        BigDecimal priceSale = originalPrice.multiply(BigDecimal.valueOf(100).subtract(discountPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
        // Làm tròn có thể ra 0 đồng dù phần trăm giảm dưới 100%; giá sale tối thiểu là 1 đồng.
        if (priceSale.signum() <= 0) {
            priceSale = BigDecimal.ONE;
        }
        return priceSale;
    }

    // Tồn kho phải có khoảng đệm gấp đôi hạn mức đăng ký; kiểm tra này không giữ hàng.
    public static void validateRegistrationStock(int stockQuantity, int registeredQuantity) {
        if (registeredQuantity <= 0 || stockQuantity < 2L * registeredQuantity) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_STOCK_INVALID);
        }
    }
}
