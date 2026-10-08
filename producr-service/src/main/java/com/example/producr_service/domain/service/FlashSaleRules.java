package com.example.producr_service.domain.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Domain service dùng chung cho chính sách giá và tồn kho khi tham gia Flash Sale.
public final class FlashSaleRules {
    private FlashSaleRules() {}

    // Tính giá VND nguyên, làm tròn xuống để đăng ký và mua dùng cùng công thức.
    public static BigDecimal calculateSalePrice(BigDecimal originalPrice, BigDecimal discountPercent) {
        if (originalPrice == null || originalPrice.signum() <= 0
                || discountPercent == null || discountPercent.signum() <= 0
                || discountPercent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new BusinessException(DomainProductError.FLASH_SALE_PRICE_INVALID);
        }
        BigDecimal priceSale = originalPrice.multiply(BigDecimal.valueOf(100).subtract(discountPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
        // nếu tính tonas giá sale <0 thì gắn giá sale = 1
        if (priceSale.signum() <= 0) {
            priceSale = BigDecimal.ONE;
        }
        return priceSale;
    }

    // Yêu cầu tồn kho ít nhất gấp đôi hạn mức đăng ký, không giữ tồn kho.
    public static void validateRegistrationStock(int stockQuantity, int registeredQuantity) {
        if (registeredQuantity <= 0 || stockQuantity < 2L * registeredQuantity) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_STOCK_INVALID);
        }
    }
}
