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

    // Khởi tạo item mới; package-private để Cart là nơi duy nhất tạo item trong aggregate.
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

    // Dựng lại item đã lưu từ dữ liệu persistence.
    public static CartItem reconstitute(Long id, Long productVariantId, int quantity,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new CartItem(id, productVariantId, quantity, createdAt, updatedAt);
    }

    // Cộng số lượng của một item.
    void increaseQuantity(int amount, LocalDateTime now) {
        requirePositive(amount);
        // Dùng long để tránh tràn int khi cộng dồn

        quantity += amount;
        updatedAt = now;
    }

    // Gán số lượng mới cho item.
    void updateQuantity(int quantity, LocalDateTime now) {
        requirePositive(quantity);
        this.quantity = quantity;
        updatedAt = now;
    }

    // Giảm số lượng item; Cart chịu trách nhiệm xóa item nếu lượng giảm chạm về 0.
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
