package com.example.producr_service.domain.model;

import com.example.producr_service.domain.error.DomainProductError;
import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.service.FlashSaleRules;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record FlashSaleItem(Long id, Long campaignId, Long variantId, Long shopId, String productName,
        List<Attribute> attributes, BigDecimal discountPercent, int registeredQuantity,
        long soldQuantity, FlashSaleItemStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public FlashSaleItem {
        if ((id != null && id <= 0) || campaignId == null || campaignId <= 0 || shopId == null || shopId <= 0
                || productName == null || productName.isBlank() || productName.length() > 255
                || registeredQuantity <= 0 || soldQuantity < 0
                || discountPercent == null || discountPercent.signum() <= 0
                || discountPercent.compareTo(BigDecimal.valueOf(100)) >= 0
                || discountPercent.stripTrailingZeros().scale() > 2) {
            throw new BusinessException(
                    DomainProductError.FLASH_SALE_ITEM_INVALID);
        }
        attributes = List.copyOf(attributes);
        Objects.requireNonNull(status, "Trạng thái đăng ký không được null");
        Objects.requireNonNull(createdAt, "Thời điểm tạo không được null");
        Objects.requireNonNull(updatedAt, "Thời điểm cập nhật không được null");
    }

    // Kiểm tra tồn kho khi đăng ký không đồng nghĩa giữ hàng cho sale.
    public static FlashSaleItem register(Long campaignId, RegistrationVariant variant,
            BigDecimal discountPercent, int registeredQuantity, LocalDateTime now) {
        if (variant.variantId() == null || variant.variantId() <= 0) {
            throw new BusinessException(
                    DomainProductError.FLASH_SALE_ITEM_INVALID);
        }
        FlashSaleRules.calculateSalePrice(variant.price(), discountPercent);
        FlashSaleRules.validateRegistrationStock(variant.stockQuantity(), registeredQuantity);
        return new FlashSaleItem(null, campaignId, variant.variantId(), variant.shopId(), variant.productName(),
                variant.attributes(), discountPercent, registeredQuantity, 0,
                FlashSaleItemStatus.REGISTERED, now, now);
    }

    // Admin vô hiệu hóa vẫn phải giữ snapshot và số lượng đã mua để bảo toàn lịch sử.
    public FlashSaleItem disable(LocalDateTime now) {
        if (status == FlashSaleItemStatus.DISABLED) {
            return this;
        }
        return new FlashSaleItem(id, campaignId, variantId, shopId, productName, attributes,
                discountPercent, registeredQuantity, soldQuantity, FlashSaleItemStatus.DISABLED, createdAt, now);
    }

    public record RegistrationVariant(Long variantId, Long shopId, String productName,
            BigDecimal price, int stockQuantity, List<Attribute> attributes) {
        public RegistrationVariant {
            attributes = List.copyOf(attributes);
        }
    }

    public record Attribute(String name, String value) {}
}
