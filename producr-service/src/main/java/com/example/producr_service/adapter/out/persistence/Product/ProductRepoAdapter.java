package com.example.producr_service.adapter.out.persistence.Product;

import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.entity.BrandEntity;
import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.adapter.mapper.ProductMapper;
import com.example.producr_service.adapter.mapper.ProductVariantMapper;
import com.example.producr_service.adapter.out.persistence.Brand.BrandJpa;
import com.example.producr_service.adapter.out.persistence.Category.CategoryJpa;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.domain.model.Product;

import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.domain.model.ProductVariant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
// Triển khai port đọc, lưu sản phẩm và phân loại bằng JPA; chỉ chuyển dữ liệu sang domain.
public class ProductRepoAdapter implements ProductRepositoryPort, ProductVariantRepoPort {
    ProductJpa productJpa;
    ProductVariantJpa productVariantJpa;
    CategoryJpa categoryJpa;
    BrandJpa brandJpa;
    ProductMapper productMapper;
    ProductVariantMapper productVariantMapper;

    // Lưu aggregate sản phẩm cùng thuộc tính, phân loại và ảnh rồi trả về domain đã có ID.
    @Override
    public Product save(Product product) {

        CategoryEntity category = categoryJpa.findById(product.getCategoryId())
                .orElseThrow(() -> new IllegalStateException(
                        "Category id=" + product.getCategoryId() + " khong ton tai (loi du lieu)"));

        BrandEntity brand = product.getBrandId() != null
                ? brandJpa.findById(product.getBrandId()).orElse(null)
                : null;

        ProductEntity entity = productMapper.toEntity(product, category, brand);

        // Cascade lưu thuộc tính, giá trị, phân loại và ảnh cùng sản phẩm.
        ProductEntity saved = productJpa.save(entity);

        return productMapper.toDomain(saved);
    }

    // Đọc một sản phẩm và chuyển toàn bộ dữ liệu liên quan sang domain.
    @Override
    public Optional<Product> findById(Long id) {
        return productJpa.findById(id).map(productMapper::toDomain);
    }

    // Kiểm tra SKU đã tồn tại trước khi tạo phân loại mới.
    @Override
    public boolean existsBySku(String sku) {
        return productVariantJpa.existsBySku(sku);
    }

    // Đọc phân loại theo các ID sản phẩm và gom thành danh sách cho từng sản phẩm.
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

    // Truy vấn một trang sản phẩm của người bán và chuyển sang domain để service xử lý.
    @Override
    public PageResponse<Product> findProductsFillter(Long sellerUserId, SellerProductFilter filter) {
        Page<ProductEntity> productEntityPage = productJpa.findSellerProducts(sellerUserId,
                filter.categoryId(),
                filter.status() == null ? null : filter.status().name(),
                ProductStatus.ACTIVE,
                ProductStatus.INACTIVE,
                filter.keyword(),
                PageRequest.of(
                        filter.pageQuery().pageIndex(),
                        filter.pageQuery().size()
                )
        );

        return  productMapper.toDomainPage(productEntityPage);
    }

}
