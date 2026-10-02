package com.example.order_service.cart.adapter.out.persistence;

import com.example.order_service.cart.adapter.entity.CartEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepo extends JpaRepository<CartEntity,Long> {
    @EntityGraph(attributePaths = "items")
    Optional<CartEntity> findByUserId(Long userId);
}
