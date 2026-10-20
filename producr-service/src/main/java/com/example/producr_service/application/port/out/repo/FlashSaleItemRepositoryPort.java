package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.application.dto.response.ProductSaleOfferView;
import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import com.example.producr_service.application.dto.response.UserFlashSaleItemView;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleItemRepositoryPort {
    List<ProductSaleOfferView> findOngoingOffersByProductIds(List<Long> productIds, LocalDateTime now);

    List<FlashSaleOfferView> findOngoingOffers(Long productId, Long variantId, LocalDateTime now);

    // Đọc ưu đãi theo lô để không truy vấn riêng cho từng item giỏ hàng.
    List<FlashSaleOfferView> findOngoingOffersByVariantIds(List<Long> variantIds, LocalDateTime now);

    Optional<FlashSaleVariantSnapshot> findVariant(Long variantId);

    // now phải cùng múi giờ DB; cố ý giữ item hết quota hoặc tồn kho để dựng trạng thái hiển thị.
    List<UserFlashSaleItemView> findItemsInOngoingCampaigns(Long categoryId, LocalDateTime now);
}
