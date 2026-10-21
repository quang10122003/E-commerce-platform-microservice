package com.example.producr_service.adapter.out.persistence.FlashSaleItem;

import com.example.producr_service.adapter.entity.FlashSaleItemEntity;
import com.example.producr_service.adapter.out.persistence.projection.ProductSaleOfferProjection;
import com.example.producr_service.adapter.out.persistence.projection.FlashSaleOfferProjection;
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

    List<FlashSaleItemEntity> findByCampaign_IdAndShopIdOrderByIdAsc(Long campaignId, Long shopId);

    @Query("""
            select p.id as productId, v.id as variantId, v.price as price, v.stockQuantity as stockQuantity,
                i.discountPercent as discountPercent, i.registeredQuantity as registeredQuantity,
                i.soldQuantity as soldQuantity, c.endsAt as endsAt
            from FlashSaleItemEntity i
            join i.campaign c join i.variant v join v.product p
            where p.id in :productIds
              and p.status = :productStatus and i.status = :itemStatus
              and c.status = :campaignStatus and c.startsAt <= :now and c.endsAt > :now
            order by i.id asc
            """)
    List<ProductSaleOfferProjection> findOngoingOffersByProductIds(
            @Param("productIds") List<Long> productIds,
            @Param("now") LocalDateTime now, @Param("productStatus") ProductStatus productStatus,
            @Param("itemStatus") FlashSaleItemStatus itemStatus,
            @Param("campaignStatus") FlashSaleCampaignStatus campaignStatus);

    // INNER JOIN cố ý loại đăng ký có variant đã bị xóa khỏi ưu đãi hiện tại.
    @Query("""
            select v.id as variantId, i.discountPercent as discountPercent,
                i.registeredQuantity as registeredQuantity, i.soldQuantity as soldQuantity, c.endsAt as endsAt
            from FlashSaleItemEntity i
            join i.campaign c join i.variant v join v.product p
            where p.id = :productId and v.id = :variantId
              and p.status = :productStatus and i.status = :itemStatus
              and c.status = :campaignStatus and c.startsAt <= :now and c.endsAt > :now
            order by i.id asc
            """)
    List<FlashSaleOfferProjection> findOngoingOffers(
            @Param("productId") Long productId, @Param("variantId") Long variantId,
            @Param("now") LocalDateTime now, @Param("productStatus") ProductStatus productStatus,
            @Param("itemStatus") FlashSaleItemStatus itemStatus,
            @Param("campaignStatus") FlashSaleCampaignStatus campaignStatus);

    @Query("""
            select v.id as variantId, i.discountPercent as discountPercent,
                i.registeredQuantity as registeredQuantity, i.soldQuantity as soldQuantity, c.endsAt as endsAt
            from FlashSaleItemEntity i
            join i.campaign c join i.variant v join v.product p
            where v.id in :variantIds
              and p.status = :productStatus and i.status = :itemStatus
              and c.status = :campaignStatus and c.startsAt <= :now and c.endsAt > :now
            order by i.id asc
            """)
    List<FlashSaleOfferProjection> findOngoingOffersByVariantIds(
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
