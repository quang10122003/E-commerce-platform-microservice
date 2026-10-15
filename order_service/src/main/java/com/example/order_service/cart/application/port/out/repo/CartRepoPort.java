package com.example.order_service.cart.application.port.out.repo;

import com.example.order_service.cart.domain.model.Cart;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface CartRepoPort {
    Optional<Cart> findByUserId(Long userId);

    Cart save(Cart cart);

    // Xóa các dòng biến thể đã mất khỏi mọi giỏ và cập nhật thời điểm thay đổi giỏ.
    int removeItemsByVariantIds(List<Long> variantIds, LocalDateTime now);
}
