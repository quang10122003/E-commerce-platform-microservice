package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.adapter.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepo extends JpaRepository<OrderItemEntity, Long> {
}
