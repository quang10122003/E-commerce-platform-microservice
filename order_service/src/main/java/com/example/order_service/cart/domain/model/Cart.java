package com.example.order_service.cart.domain.model;

import com.example.common.exception.BusinessException;
import com.example.order_service.cart.domain.error.CartDomainError;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public class Cart {

    private final Long id;
    private final Long userId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<CartItem> items;

    private Cart(Long id, Long userId, LocalDateTime createdAt,
                 LocalDateTime updatedAt, List<CartItem> items) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(CartDomainError.USER_ID_REQUIRED);
        }
        this.id = id;
        this.userId = userId;         
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = new ArrayList<>(items == null ? List.of() : items);
    }

    public static Cart create(Long userId, LocalDateTime now) {
        return new Cart(null, userId, now, now, new ArrayList<>());
    }

    public static Cart reconstitute(Long id, Long userId, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, List<CartItem> items) {
        return new Cart(id, userId, createdAt, updatedAt,
                items == null ? new ArrayList<>() : items);
    }
    public void addItem(Long productVariantId, LocalDateTime now) {
        addItem(productVariantId, 1, now);
    }

    public void addItem(Long productVariantId, int quantity, LocalDateTime now) {
        if (productVariantId == null || productVariantId <= 0) {
            throw new BusinessException(CartDomainError.PRODUCT_VARIANT_ID_REQUIRED);
        }
        if (quantity <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }

        Optional<CartItem> existing = findItem(productVariantId);
        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity, now);
        } else {
            items.add(new CartItem(null, productVariantId, quantity, now, now));
        }
        updatedAt = now;
    }

    public void updateItemQuantity(Long productVariantId, int quantity, LocalDateTime now) {
        if (quantity <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }
        CartItem item = getItemOrThrow(productVariantId);
        item.updateQuantity(quantity, now);
        updatedAt = now;
    }

    public void removeItem(Long productVariantId, LocalDateTime now) {
        CartItem item = getItemOrThrow(productVariantId);
        items.remove(item);
        updatedAt = now;
    }

    public void decreaseItemQuantity(Long productVariantId, int amount, LocalDateTime now) {
        if (amount <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }
        CartItem item = getItemOrThrow(productVariantId);
        if (amount >= item.getQuantity()) {
            items.remove(item);
        } else {
            item.decreaseQuantity(amount, now);
        }
        updatedAt = now;
    }

    public void clear(LocalDateTime now) {
        items.clear();
        updatedAt = now;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private Optional<CartItem> findItem(Long productVariantId) {
        if (productVariantId == null || productVariantId <= 0) {
            throw new BusinessException(CartDomainError.PRODUCT_VARIANT_ID_REQUIRED);
        }
        return items.stream()
                .filter(i -> i.getProductVariantId().equals(productVariantId))
                .findFirst();
    }

    private CartItem getItemOrThrow(Long productVariantId) {
        return findItem(productVariantId)
                .orElseThrow(() -> new BusinessException(CartDomainError.ITEM_NOT_FOUND));
    }
}
