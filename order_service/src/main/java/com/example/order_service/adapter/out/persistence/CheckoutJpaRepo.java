package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.CheckoutEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CheckoutJpaRepo extends JpaRepository<CheckoutEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CheckoutEntity c where c.id = :checkoutId")
    Optional<CheckoutEntity> findByIdForUpdate(@Param("checkoutId") Long checkoutId);

    Optional<CheckoutEntity> findByIdempotencyKey(String idempotencyKey);
}
