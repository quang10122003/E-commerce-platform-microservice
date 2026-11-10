package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.PaymentEntity;
import com.example.order_service.domain.model.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepo extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByCheckoutId(Long checkoutId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentEntity p where p.checkout.id = :checkoutId")
    Optional<PaymentEntity> findByCheckoutIdForUpdate(@Param("checkoutId") Long checkoutId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentEntity p where p.transactionReference = :transactionReference")
    Optional<PaymentEntity> findByTransactionReferenceForUpdate(
            @Param("transactionReference") String transactionReference);

    @Query("select p.transactionReference from PaymentEntity p "
            + "where p.status = :status and p.expiresAt < :cutoff order by p.expiresAt")
    List<String> findReferencesByStatusExpiredBefore(@Param("status") PaymentStatus status,
            @Param("cutoff") LocalDateTime cutoff, Pageable pageable);
}
