package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.order_service.domain.error.OrderDomainError;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class OrderItem {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999999999");

    private final Long id;
    private final Long productId;
    private final Long productVariantId;
    private final String productName;
    private final String variantAttributes;
    private final String imageUrl;
    private final int quantity;
    private final BigDecimal originalUnitPrice;
    private final BigDecimal unitPrice;
    private final BigDecimal totalAmount;
    private final PriceType priceType;
    private final LocalDateTime createdAt;

    private OrderItem(Long id, Long productId, Long productVariantId, String productName,
                      String variantAttributes, String imageUrl, int quantity,
                      BigDecimal originalUnitPrice, BigDecimal unitPrice, PriceType priceType,
                      LocalDateTime createdAt) {
        if (quantity <= 0) {
            throw new BusinessException(OrderDomainError.INVALID_QUANTITY);
        }
        this.priceType = Objects.requireNonNull(priceType);
        this.originalUnitPrice = validateAmount(originalUnitPrice);
        this.unitPrice = validateAmount(unitPrice);
        if (unitPrice.compareTo(originalUnitPrice) > 0
                || (priceType == PriceType.NORMAL && unitPrice.compareTo(originalUnitPrice) != 0)) {
            throw new BusinessException(OrderDomainError.INVALID_PRICE);
        }
        this.id = validateOptionalId(id);
        this.productId = validateOptionalId(productId);
        this.productVariantId = validateOptionalId(productVariantId);
        this.productName = requireText(productName, 255);
        this.variantAttributes = requireText(variantAttributes, Integer.MAX_VALUE);
        this.imageUrl = validateOptionalText(imageUrl, 2048);
        this.quantity = quantity;
        this.totalAmount = validateAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        this.createdAt = validateTime(createdAt, null);
    }

    public static OrderItem create(Long productId, Long productVariantId, String productName,
                                   String variantAttributes, String imageUrl, int quantity,
                                   BigDecimal originalUnitPrice, BigDecimal unitPrice,
                                   PriceType priceType, LocalDateTime now) {
        requireId(productId);
        requireId(productVariantId);
        return new OrderItem(null, productId, productVariantId, productName, variantAttributes,
                imageUrl, quantity, originalUnitPrice, unitPrice, priceType, now);
    }

    public static OrderItem reconstitute(Long id, Long productId, Long productVariantId,
                                         String productName, String variantAttributes, String imageUrl,
                                         int quantity, BigDecimal originalUnitPrice, BigDecimal unitPrice,
                                         BigDecimal totalAmount, PriceType priceType, LocalDateTime createdAt) {
        requireId(id);
        OrderItem item = new OrderItem(id, productId, productVariantId, productName, variantAttributes,
                imageUrl, quantity, originalUnitPrice, unitPrice, priceType, createdAt);
        if (item.totalAmount.compareTo(validateAmount(totalAmount)) != 0) {
            throw new BusinessException(OrderDomainError.TOTAL_MISMATCH);
        }
        return item;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getProductVariantId() {
        return productVariantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantAttributes() {
        return variantAttributes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getOriginalUnitPrice() {
        return originalUnitPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private static BigDecimal validateAmount(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(MAX_AMOUNT) > 0
                || value.stripTrailingZeros().scale() > 0) {
            throw new BusinessException(OrderDomainError.INVALID_AMOUNT);
        }
        return value;
    }

    private static Long requireId(Long value) {
        if (value == null || value <= 0) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }

    private static Long validateOptionalId(Long value) {
        return value == null ? null : requireId(value);
    }

    private static String requireText(String value, int maxLength) {
        if (!ValidationUtils.hasText(value) || value.length() > maxLength) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }

    private static String validateOptionalText(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }

    private static LocalDateTime validateTime(LocalDateTime value, LocalDateTime earliest) {
        if (value == null || (earliest != null && value.isBefore(earliest))) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }
}
