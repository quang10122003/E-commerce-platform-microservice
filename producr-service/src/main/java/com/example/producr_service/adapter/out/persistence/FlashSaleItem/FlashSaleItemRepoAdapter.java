package com.example.producr_service.adapter.out.persistence.FlashSaleItem;

import com.example.producr_service.adapter.mapper.FlashSaleItemMapper;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FlashSaleItemRepoAdapter implements FlashSaleItemRepositoryPort {
    private final ProductVariantJpa productVariantJpa;
    private final FlashSaleItemMapper flashSaleItemMapper;

    // Đọc sản phẩm và variant hiện có để tạo snapshot, không giữ khóa khi kiểm tra.
    @Override
    public Optional<FlashSaleVariantSnapshot> findVariant(Long variantId) {
        return productVariantJpa.findSnapshotById(variantId).map(flashSaleItemMapper::toSnapshot);
    }

}
