package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.application.dto.checkout.CheckoutSaleDto;
import com.example.producr_service.application.dto.response.ProductSaleOfferView;
import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import com.example.producr_service.application.dto.response.UserFlashSaleItemView;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.application.dto.response.VariantDiscountView;
import com.example.producr_service.domain.model.FlashSaleItem;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleItemRepositoryPort {
    List<Long> findCheckoutCandidates(List<Long> variantIds, LocalDateTime now);

    List<Long> findCampaignIdsByItemIds(List<Long> itemIds);

    List<CheckoutSaleDto> lockItemsForUpdate(List<Long> itemIds);

    void updateSoldQuantity(Long itemId, long soldQuantity, LocalDateTime now);

    List<VariantDiscountView> findPublishedDiscountsByVariantIds(List<Long> variantIds, LocalDateTime now);

    List<FlashSaleItem> findShopItems(Long campaignId, Long shopId);

    List<FlashSaleVariantSnapshot> findVariants(Collection<Long> variantIds);

    List<ProductSaleOfferView> findOngoingOffersByProductIds(List<Long> productIds, LocalDateTime now);

    List<FlashSaleOfferView> findOngoingOffers(Long productId, Long variantId, LocalDateTime now);

    // Đọc ưu đãi theo lô để không truy vấn riêng cho từng item giỏ hàng.
    List<FlashSaleOfferView> findOngoingOffersByVariantIds(List<Long> variantIds, LocalDateTime now);

    Optional<FlashSaleVariantSnapshot> findVariant(Long variantId);

    // now phải cùng múi giờ DB; cố ý giữ item hết quota hoặc tồn kho để dựng trạng thái hiển thị.
    List<UserFlashSaleItemView> findItemsInOngoingCampaigns(Long categoryId, LocalDateTime now);
}
