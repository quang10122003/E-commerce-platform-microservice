package com.example.producr_service.adapter.out.persistence.FlashSaleItem;

import com.example.producr_service.adapter.entity.FlashSaleItemEntity;
import com.example.producr_service.adapter.out.persistence.projection.ProductSaleOfferProjection;
import com.example.producr_service.adapter.out.persistence.projection.FlashSaleOfferProjection;
import com.example.producr_service.adapter.out.persistence.projection.VariantDiscountProjection;
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
    @Query("""
            select i.id
            from FlashSaleItemEntity i join i.campaign c
            where i.variant.id in :variantIds and i.status = 'REGISTERED' and c.status = 'PUBLISHED'
              and c.startsAt <= :now and c.endsAt > :now
            order by c.id, i.id
            """)
    List<Long> findCheckoutCandidates(
            @Param("variantIds") List<Long> variantIds, @Param("now") LocalDateTime now);

    @Query("select distinct i.campaign.id from FlashSaleItemEntity i where i.id in :ids order by i.campaign.id")
    List<Long> findCampaignIdsByItemIds(@Param("ids") List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from FlashSaleItemEntity i where i.id = :id")
    Optional<FlashSaleItemEntity> findByIdForUpdate(@Param("id") Long id);

    // Không chờ job đổi trạng thái để ngừng áp dụng sale đã hết giờ.
    @Query("""
            select i.variant.id as variantId, i.discountPercent as discountPercent,
                i.registeredQuantity as registeredQuantity, i.soldQuantity as soldQuantity
            from FlashSaleItemEntity i join i.campaign c
            where i.variant.id in :variantIds and i.status = :itemStatus and c.status = :campaignStatus
              and c.startsAt <= :now
              and c.endsAt > :now
            """)
    List<VariantDiscountProjection> findPublishedDiscountsByVariantIds(
            @Param("variantIds") List<Long> variantIds, @Param("itemStatus") FlashSaleItemStatus itemStatus,
            @Param("campaignStatus") FlashSaleCampaignStatus campaignStatus, @Param("now") LocalDateTime now);


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

    @Query("""
            select v.id as variantId, i.discountPercent as discountPercent,
                i.registeredQuantity as registeredQuantity, i.soldQuantity as soldQuantity, c.endsAt as endsAt
            from FlashSaleItemEntity i
            join i.campaign c join i.variant v join v.product p
            where p.status = :productStatus and i.status = :itemStatus
              and c.status = :campaignStatus and c.startsAt <= :now and c.endsAt > :now
            order by i.id asc
            """)
    List<FlashSaleOfferProjection> findAllOngoingOffers(
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
              and p.shopLock = false
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
