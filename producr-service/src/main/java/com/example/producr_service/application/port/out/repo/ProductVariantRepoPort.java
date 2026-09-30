package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductVariantRepoPort {
    boolean existsBySku(String sku);

    Map<Long, List<ProductVariant>> findByProduct_IdIn(Collection<Long> productIds);

    // lấy ttoàn bộ phân loại sản phẩm dựa vào proddcut id và khóa các bản ghi
    List<ProductVariant> findByProductIdForUpdate(Long productId);

    Optional<ProductVariant> findByProductIdAndIdForUpdate(Long productId, Long variantId);

    long countByProduct_Id(Long productId);

    List<StoredFile> findStoredFilesByVariantIds(Collection<Long> variantIds);

    void deleteById(Long variantId);
}
