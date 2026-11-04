package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepo extends JpaRepository<OrderItemEntity, Long> {
}
