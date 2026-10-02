package com.example.order_service.cart.adapter.mapper;

import com.example.order_service.cart.adapter.entity.CartEntity;
import com.example.order_service.cart.adapter.entity.CartItemEntity;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // Dựng aggregate Cart từ entity, bao gồm toàn bộ item đã lưu.
    default Cart toDomain(CartEntity entity) {
        if (entity == null) {
            return null;
        }

        List<CartItem> items = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream()
                        .map(this::toDomain)
                        .toList();

        return Cart.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                items
        );
    }

    // Dựng CartItem domain từ entity item đã lưu.
    default CartItem toDomain(CartItemEntity entity) {
        if (entity == null) {
            return null;
        }

        return CartItem.reconstitute(
                entity.getId(),
                entity.getProductVariantId(),
                entity.getQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Map Cart sang entity và thiết lập quan hệ hai chiều với các item.
    default CartEntity toEntity(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartEntity entity = toEntityWithoutItems(cart);
        List<CartItemEntity> items = cart.getItems().stream()
                .map(this::toEntity)
                .toList();
        items.forEach(item -> item.setCart(entity));
        entity.setItems(new ArrayList<>(items));
        return entity;
    }

    // Để MapStruct map các trường Cart, bỏ qua danh sách item cần gắn quan hệ riêng.
    @Mapping(target = "items", ignore = true)
    CartEntity toEntityWithoutItems(Cart cart);

    // Để MapStruct map CartItem; quan hệ cart được gắn sau khi map.
    @Mapping(target = "cart", ignore = true)
    CartItemEntity toEntity(CartItem item);
}
