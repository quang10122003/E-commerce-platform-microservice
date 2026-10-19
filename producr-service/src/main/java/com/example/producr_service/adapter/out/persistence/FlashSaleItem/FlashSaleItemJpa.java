package com.example.producr_service.adapter.out.persistence.FlashSaleItem;

import com.example.producr_service.adapter.entity.FlashSaleItemEntity;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import com.example.producr_service.domain.model.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleItemJpa extends JpaRepository<FlashSaleItemEntity, Long> {
    // INNER JOIN cố ý loại đăng ký có variant đã bị xóa khỏi ưu đãi hiện tại.
    @Query("""
            select new com.example.producr_service.application.dto.response.FlashSaleOfferView(
                v.id, i.discountPercent, i.registeredQuantity, i.soldQuantity, c.endsAt)
            from FlashSaleItemEntity i
            join i.campaign c join i.variant v join v.product p
            where p.id = :productId and v.id = :variantId
              and p.status = :productStatus and i.status = :itemStatus
              and c.status = :campaignStatus and c.startsAt <= :now and c.endsAt > :now
            order by i.id asc
            """)
    List<FlashSaleOfferView> findOngoingOffers(
            @Param("productId") Long productId, @Param("variantId") Long variantId,
            @Param("now") LocalDateTime now, @Param("productStatus") ProductStatus productStatus,
            @Param("itemStatus") FlashSaleItemStatus itemStatus,
            @Param("campaignStatus") FlashSaleCampaignStatus campaignStatus);

    @Query("""
            select new com.example.producr_service.application.dto.response.FlashSaleOfferView(
                v.id, i.discountPercent, i.registeredQuantity, i.soldQuantity, c.endsAt)
            from FlashSaleItemEntity i
            join i.campaign c join i.variant v join v.product p
            where v.id in :variantIds
              and p.status = :productStatus and i.status = :itemStatus
              and c.status = :campaignStatus and c.startsAt <= :now and c.endsAt > :now
            order by i.id asc
            """)
    List<FlashSaleOfferView> findOngoingOffersByVariantIds(
            @Param("variantIds") List<Long> variantIds,
            @Param("now") LocalDateTime now, @Param("productStatus") ProductStatus productStatus,
            @Param("itemStatus") FlashSaleItemStatus itemStatus,
            @Param("campaignStatus") FlashSaleCampaignStatus campaignStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FlashSaleItemEntity> findByCampaign_IdAndVariant_Id(Long campaignId, Long variantId);

    @Query("""
            select i
            from FlashSaleItemEntity i
            join fetch i.campaign c
            join fetch i.variant v
            join fetch v.product p
            where c.status = :campaignStatus
              and c.startsAt <= :now
              and c.endsAt > :now
              and i.status = :itemStatus
              and p.status = :productStatus
              and (:categoryId is null or p.category.id = :categoryId)
            order by i.id asc
            """)
    List<FlashSaleItemEntity> findItemsInOngoingCampaigns(
            @Param("categoryId") Long categoryId,
            @Param("now") LocalDateTime now,
            @Param("campaignStatus") FlashSaleCampaignStatus campaignStatus,
            @Param("itemStatus") FlashSaleItemStatus itemStatus,
            @Param("productStatus") ProductStatus productStatus
    );
}
