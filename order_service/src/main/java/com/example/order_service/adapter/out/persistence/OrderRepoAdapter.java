package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderEntity;
import com.example.order_service.adapter.entity.OrderItemEntity;
import com.example.order_service.adapter.mapper.OrderMapper;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepoAdapter implements OrderRepoPort {
    private final OrderJpaRepo orders;
    private final OrderItemJpaRepo items;
    private final CheckoutJpaRepo checkouts;
    private final OrderMapper mapper;

    @Override
    public List<Long> saveAll(List<Order> values) {
        List<Long> ids = new ArrayList<>();
        for (Order value : values) {
            OrderEntity entity = mapper.toEntity(value);
            entity.setCheckout(checkouts.getReferenceById(value.getCheckoutId()));
            OrderEntity saved = orders.save(entity);
            List<OrderItemEntity> itemEntities = new ArrayList<>();
            for (OrderItem item : value.getItems()) {
                OrderItemEntity itemEntity = mapper.toEntity(item);
                itemEntity.setOrder(saved);
                itemEntities.add(itemEntity);
            }
            items.saveAll(itemEntities);
            ids.add(saved.getId());
        }
        return List.copyOf(ids);
    }

    @Override
    public List<Order> findByCheckoutIdForUpdate(Long checkoutId) {
        return orders.findByCheckoutIdForUpdate(checkoutId).stream().map(mapper::toDomain).toList();
    }

    // Chỉ ghi các trường trạng thái; item của order không đổi nên không ghi lại.
    @Override
    public void saveStatus(List<Order> values) {
        for (Order value : values) {
            OrderEntity entity = orders.getReferenceById(value.getId());
            entity.setStatus(value.getStatus());
            entity.setCancellationReason(value.getCancellationReason());
            entity.setCancelledAt(value.getCancelledAt());
            entity.setUpdatedAt(value.getUpdatedAt());
        }
    }
}
