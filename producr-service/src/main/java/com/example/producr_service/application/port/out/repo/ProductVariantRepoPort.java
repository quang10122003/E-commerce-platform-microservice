package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.application.dto.response.VariantCartView;
import com.example.producr_service.application.dto.response.VariantPriceView;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductVariantRepoPort {
    Optional<Long> findProductIdByVariantId(Long variantId);

    // Đọc giá và tồn kho theo cả product ID và variant ID, không khóa hoặc tải ảnh.
    Optional<VariantPriceView> findPriceView(Long productId, Long variantId);

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long variantId);

    Map<Long, List<ProductVariant>> findByProduct_IdIn(Collection<Long> productIds);

    // lấy ttoàn bộ phân loại sản phẩm dựa vào proddcut id và khóa các bản ghi
    List<ProductVariant> findByProductIdForUpdate(Long productId);

    Optional<ProductVariant> findByProductIdAndIdForUpdate(Long productId, Long variantId);

    long countByProduct_Id(Long productId);

    List<StoredFile> findStoredFilesByVariantIds(Collection<Long> variantIds);

    void deleteById(Long variantId);
/// Trả dữ liệu thô phục vụ giỏ hàng
    List<VariantCartView> findCartViewsByVariantIds(Collection<Long> variantIds);
}
