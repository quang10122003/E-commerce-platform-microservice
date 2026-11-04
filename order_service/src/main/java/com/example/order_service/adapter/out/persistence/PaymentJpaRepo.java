package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.PaymentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentJpaRepo extends JpaRepository<PaymentEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentEntity p where p.checkout.id = :checkoutId")
    Optional<PaymentEntity> findByCheckoutIdForUpdate(@Param("checkoutId") Long checkoutId);
}
