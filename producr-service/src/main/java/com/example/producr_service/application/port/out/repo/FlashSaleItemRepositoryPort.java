package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import java.util.Optional;

public interface FlashSaleItemRepositoryPort {
    // Đọc thông tin variant và thuộc tính để tạo snapshot, không khóa dữ liệu.
    Optional<FlashSaleVariantSnapshot> findVariant(Long variantId);
}
