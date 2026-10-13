package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import com.example.producr_service.application.dto.response.UserFlashSaleItemView;
import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleItemRepositoryPort {
    // Đọc ưu đãi REGISTERED trong campaign đang chạy của đúng variant, không tải toàn bộ sale.
    List<FlashSaleOfferView> findOngoingOffers(Long productId, Long variantId, LocalDateTime now);

    // Đọc thông tin variant và thuộc tính để tạo snapshot, không khóa dữ liệu.
    Optional<FlashSaleVariantSnapshot> findVariant(Long variantId);

    // Đọc item REGISTERED có variant và product ACTIVE, thuộc sale PUBLISHED đang chạy; không phân trang hoặc khóa dữ liệu.
    // categoryId null thì không lọc; now dùng cùng múi giờ DB, giữ cả item hết quota hoặc tồn kho.
    List<UserFlashSaleItemView> findItemsInOngoingCampaigns(Long categoryId, LocalDateTime now);
}
