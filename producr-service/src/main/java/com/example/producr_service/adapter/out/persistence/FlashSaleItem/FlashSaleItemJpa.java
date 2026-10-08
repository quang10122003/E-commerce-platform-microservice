package com.example.producr_service.adapter.out.persistence.FlashSaleItem;

import com.example.producr_service.adapter.entity.FlashSaleItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

public interface FlashSaleItemJpa extends JpaRepository<FlashSaleItemEntity, Long> {
    // Giữ cách kiểm tra trùng có khóa của luồng đăng ký hiện tại.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FlashSaleItemEntity> findByCampaign_IdAndVariant_Id(Long campaignId, Long variantId);
}
