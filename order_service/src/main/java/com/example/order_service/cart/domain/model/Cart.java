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

    public static final int MAX_QUANTITY_PER_ITEM = 99;

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
        this.userId = userId;          // <-- thêm dòng này
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = new ArrayList<>(items == null ? List.of() : items);
    }

    // Tạo giỏ mới với danh sách sản phẩm rỗng.
    public static Cart create(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return new Cart(null, userId, now, now, List.of());
    }

    // Dựng aggregate từ dữ liệu đã lưu.
    public static Cart reconstitute(Long id, Long userId, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, List<CartItem> items) {
        return new Cart(id, userId, createdAt, updatedAt, items == null ? List.of() : items);
    }

    // Thêm biến thể mới hoặc cộng dồn số lượng của biến thể đã có.
    public void addItem(Long productVariantId, int quantity) {
        if (productVariantId == null || productVariantId <= 0) {
            throw new BusinessException(CartDomainError.PRODUCT_VARIANT_ID_REQUIRED);
        }
        if (quantity <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<CartItem> existing = findItem(productVariantId);
        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
        } else {
            items.add(new CartItem(null, productVariantId, quantity, now, now));
        }
        updatedAt = now;
    }

    // Xóa biến thể khỏi giỏ; báo lỗi nếu giỏ không chứa biến thể đó.
    public void removeItem(Long productVariantId) {
        CartItem item = getItemOrThrow(productVariantId);
        items.remove(item);
        updatedAt = LocalDateTime.now();
    }

    // Giảm số lượng item hoặc xóa item nếu số lượng giảm chạm về 0.
    public void decreaseItemQuantity(Long productVariantId, int amount) {
        if (amount <= 0) {
            throw new BusinessException(CartDomainError.QUANTITY_INVALID);
        }
        CartItem item = getItemOrThrow(productVariantId);
        if (amount >= item.getQuantity()) {
            items.remove(item);
        } else {
            item.decreaseQuantity(amount);
        }
        updatedAt = LocalDateTime.now();
    }

    // Xóa toàn bộ item khỏi giỏ.
    public void clear() {
        items.clear();
        updatedAt = LocalDateTime.now();
    }

    // Trả về danh sách chỉ đọc để caller không sửa state bên trong aggregate.
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
