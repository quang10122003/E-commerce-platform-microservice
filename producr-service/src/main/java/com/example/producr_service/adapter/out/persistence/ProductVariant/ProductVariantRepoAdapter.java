package com.example.producr_service.adapter.out.persistence.ProductVariant;

import com.example.producr_service.adapter.mapper.ProductVariantMapper;
import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.entity.VariantImageEntity;
import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.dto.response.VariantCartView;
import com.example.producr_service.application.dto.response.VariantPriceView;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.model.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductVariantRepoAdapter implements ProductVariantRepoPort {
    private final ProductVariantJpa productVariantJpa;
    private final ProductVariantMapper productVariantMapper;

    // Đọc projection giá và tồn kho, không dựng aggregate hoặc tải collection.
    @Override
    public Optional<VariantPriceView> findPriceView(Long productId, Long variantId) {
        return productVariantJpa.findPriceView(productId, variantId)
                .map(productVariantMapper::toPriceView);
    }

    // Kiểm tra SKU đã tồn tại trước khi tạo phân loại mới.
    @Override
    public boolean existsBySku(String sku) {
        return productVariantJpa.existsBySku(sku);
    }

    // Kiểm tra trùng SKU nhưng bỏ qua chính bản ghi đang được cập nhật.
    @Override
    public boolean existsBySkuAndIdNot(String sku, Long variantId) {
        return productVariantJpa.existsBySkuAndIdNot(sku, variantId);
    }

    // Gom các phân loại theo sản phẩm để dựng trang người bán.
    @Override
    public Map<Long, List<ProductVariant>> findByProduct_IdIn(Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return productVariantJpa.findByProduct_IdIn(productIds).stream()
                .collect(Collectors.groupingBy(
                        variant -> variant.getProduct().getId(),
                        Collectors.mapping(productVariantMapper::toSellerDomain, Collectors.toList())
                ));
    }

    // Khóa các phân loại theo thứ tự ID trước khi xóa sản phẩm.
    @Override
    public List<ProductVariant> findByProductIdForUpdate(Long productId) {
        return productVariantJpa.findByIdForLock(productId).stream()
                .map(productVariantMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProductVariant> findByProductIdAndIdForUpdate(Long productId, Long variantId) {
        return productVariantJpa.findByProductIdAndIdForUpdate(productId, variantId)
                .map(productVariantMapper::toDomain);
    }

    // lấy số ProductVariant có productId
    @Override
    public long countByProduct_Id(Long productId) {
        return  productVariantJpa.countByProduct_Id(productId);
    }

    // Đọc metadata ảnh theo danh sách phân loại và giữ nguyên bucket đã lưu.
    @Override
    public List<StoredFile> findStoredFilesByVariantIds(Collection<Long> variantIds) {
        if (variantIds.isEmpty()) {
            return List.of();
        }
        return productVariantJpa.findAllById(variantIds).stream()
                .flatMap(variant -> variant.getImages().stream())
                .map(this::toStoredFile)
                .toList();
    }

    private StoredFile toStoredFile(VariantImageEntity image) {
        return new StoredFile(image.getStorageBucket(), image.getObjectPath(), image.getImageUrl());
    }

    // Xóa phân loại; quan hệ ảnh và bảng nối được dọn trong cùng transaction.
    @Override
    public void deleteById(Long variantId) {
        productVariantJpa.deleteById(variantId);
    }
//   / Đọc dữ liệu thô của variant và product
    @Override
    public List<VariantCartView> findCartViewsByVariantIds(Collection<Long> variantIds) {
        if (variantIds.isEmpty()) {
            return List.of();
        }

        List<ProductVariantEntity> variants = productVariantJpa.findAllById(variantIds);
        return variants.stream()
                .map(productVariantMapper::toCartView)
                .toList();
    }

}
