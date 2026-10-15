package com.example.producr_service.adapter.out.persistence.FlashSaleItem;

import com.example.producr_service.adapter.mapper.FlashSaleItemMapper;
import com.example.producr_service.adapter.mapper.ProductVariantMapper;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import com.example.producr_service.application.dto.response.UserFlashSaleItemView;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import com.example.producr_service.domain.model.ProductStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class FlashSaleItemRepoAdapter implements FlashSaleItemRepositoryPort {
     ProductVariantJpa productVariantJpa;
     FlashSaleItemMapper flashSaleItemMapper;
     FlashSaleItemJpa flashSaleItemJpa;
     ProductVariantMapper productVariantMapper;

    // Truy vấn projection ưu đãi, không khóa dữ liệu hoặc xử lý quota tại adapter.
    @Override
    public List<FlashSaleOfferView> findOngoingOffers(Long productId, Long variantId, LocalDateTime now) {
        return flashSaleItemJpa.findOngoingOffers(productId, variantId, now, ProductStatus.ACTIVE,
                FlashSaleItemStatus.REGISTERED, FlashSaleCampaignStatus.PUBLISHED);
    }

    // Đọc ưu đãi theo danh sách variant, bỏ qua truy vấn khi danh sách rỗng.
    @Override
    public List<FlashSaleOfferView> findOngoingOffersByVariantIds(List<Long> variantIds, LocalDateTime now) {
        if (variantIds.isEmpty()) {
            return List.of();
        }
        return flashSaleItemJpa.findOngoingOffersByVariantIds(variantIds, now, ProductStatus.ACTIVE,
                FlashSaleItemStatus.REGISTERED, FlashSaleCampaignStatus.PUBLISHED);
    }

    // Đọc sản phẩm và variant hiện có để tạo snapshot, không giữ khóa khi kiểm tra.
    @Override
    public Optional<FlashSaleVariantSnapshot> findVariant(Long variantId) {
        return productVariantJpa.findSnapshotById(variantId).map(flashSaleItemMapper::toSnapshot);
    }

    @Override
    public List<UserFlashSaleItemView> findItemsInOngoingCampaigns(Long categoryId, LocalDateTime now) {
        // lấy ra thông tin các sản phẩm đang trong sale và k bị admin lock và k bị shop khóa fillter theo category
         return flashSaleItemJpa.findItemsInOngoingCampaigns(
                categoryId,now, FlashSaleCampaignStatus.PUBLISHED, FlashSaleItemStatus.REGISTERED, ProductStatus.ACTIVE
        ).stream().map(flashSaleItemMapper::toUserFlashSaleItemView).toList();
    }

}
