package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.adapter.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepo extends JpaRepository<OrderEntity, Long> {
}
