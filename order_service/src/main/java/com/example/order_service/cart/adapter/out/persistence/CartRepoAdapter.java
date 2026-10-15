package com.example.order_service.cart.adapter.out.persistence;

import com.example.order_service.cart.adapter.mapper.CartMapper;
import com.example.order_service.cart.application.port.out.repo.CartRepoPort;
import com.example.order_service.cart.domain.model.Cart;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class CartRepoAdapter implements CartRepoPort {
    CartJpaRepo cartJpaRepo;
    CartMapper cartMapper;

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return cartJpaRepo.findByUserId(userId).map(cartMapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        return cartMapper.toDomain(cartJpaRepo.save(cartMapper.toEntity(cart)));
    }

    // Thực hiện cập nhật timestamp và xóa item bằng transaction do application quản lý.
    @Override
    public int removeItemsByVariantIds(List<Long> variantIds, LocalDateTime now) {
        if (variantIds.isEmpty()) {
            return 0;
        }
        cartJpaRepo.updateCartsContainingVariants(variantIds, now);
        return cartJpaRepo.deleteItemsByVariantIds(variantIds);
    }
}
