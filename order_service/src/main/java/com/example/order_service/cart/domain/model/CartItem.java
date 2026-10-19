package com.example.order_service.cart.domain.model;

import com.example.common.exception.BusinessException;
import com.example.order_service.cart.domain.error.CartDomainError;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CartItem {

    private final Long id;
    private final Long productVariantId;
    private int quantity;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor giới hạn trong package để Cart kiểm soát việc tạo item của aggregate.
    CartItem(Long id, Long productVariantId, int quantity,
             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        if (productVariantId == null || productVariantId <= 0) {
            throw new BusinessException(CartDomainError.PRODUCT_VARIANT_ID_REQUIRED);
        }
        this.productVariantId = productVariantId;
        if (quantity <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CartItem reconstitute(Long id, Long productVariantId, int quantity,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new CartItem(id, productVariantId, quantity, createdAt, updatedAt);
    }

    void increaseQuantity(int amount, LocalDateTime now) {
        requirePositive(amount);
        // Dùng long để tránh tràn int khi cộng dồn

        quantity += amount;
        updatedAt = now;
    }

    void updateQuantity(int quantity, LocalDateTime now) {
        requirePositive(quantity);
        this.quantity = quantity;
        updatedAt = now;
    }

    // Cart chịu trách nhiệm xóa item khi số lượng về 0; item không tự rời aggregate.
    void decreaseQuantity(int amount, LocalDateTime now) {
        requirePositive(amount);
        if (amount >= quantity) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }
        quantity -= amount;
        updatedAt = now;
    }

    private static void requirePositive(int value) {
        if (value <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }
    }
}
