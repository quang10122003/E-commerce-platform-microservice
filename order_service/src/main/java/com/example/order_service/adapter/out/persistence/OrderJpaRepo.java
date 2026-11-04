package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepo extends JpaRepository<OrderEntity, Long> {
}
