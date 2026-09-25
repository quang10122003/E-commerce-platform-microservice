package com.example.producr_service.adapter.out.persistence.Product;

import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.entity.BrandEntity;
import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.mapper.ProductMapper;
import com.example.producr_service.adapter.mapper.ProductVariantMapper;
import com.example.producr_service.adapter.out.persistence.Brand.BrandJpa;
import com.example.producr_service.adapter.out.persistence.Category.CategoryJpa;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.repo.SellerProductQueryPort;
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
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)

public class ProductRepoAdapter implements ProductRepositoryPort , SellerProductQueryPort , ProductVariantRepoPort {
    ProductJpa productJpa;
    ProductVariantJpa productVariantJpa;
    CategoryJpa categoryJpa;
    BrandJpa brandJpa;
    ProductMapper productMapper;
    ProductVariantMapper productVariantMapper;

    @Override
    public Product save(Product product) {

        CategoryEntity category = categoryJpa.findById(product.getCategoryId())
                .orElseThrow(() -> new IllegalStateException(
                        "Category id=" + product.getCategoryId() + " khong ton tai (loi du lieu)"));

        BrandEntity brand = product.getBrandId() != null
                ? brandJpa.findById(product.getBrandId()).orElse(null)
                : null;

        ProductEntity entity = productMapper.toEntity(product, category, brand);

        // save() cascade xuong attributes, attribute_values, variants,
        // variant_images va tu dong INSERT bang noi variant_attribute_values
        ProductEntity saved = productJpa.save(entity);

        return productMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productJpa.findById(id).map(productMapper::toDomain);
    }

    // check sku tồn tại chưa
    @Override
    public boolean existsBySku(String sku) {
        return productVariantJpa.existsBySku(sku);
    }

    @Override
    public Map<Long, List<ProductVariant>> findByProduct_IdIn(Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return productVariantJpa.findByProduct_IdIn(productIds).stream()
                .collect(Collectors.groupingBy(
                        variant -> variant.getProduct().getId(),
                        Collectors.mapping(productVariantMapper::toDomain, Collectors.toList())
                ));
    }

    // Lấy trang sản phẩm và gắn các phân loại đã tải vào từng sản phẩm.
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerProductItemResponse> findProducts(Long sellerUserId, SellerProductFilter filter) {
        Page<ProductEntity> productPage = productJpa.findSellerProducts(sellerUserId,
                filter.categoryId(),
                filter.status() == null ? null : filter.status().name(),
                ProductStatus.ACTIVE,
                ProductStatus.INACTIVE,
                PageRequest.of(
                        filter.pageQuery().pageIndex(),
                        filter.pageQuery().size()
                )
        );
        // Lấy ID trang hiện tại để truy vấn thuộc tính phân loại theo lô, tránh N+1.
        List<Long> productIds = productPage.getContent().stream()
                .map(ProductEntity::getId)
                .toList();

        // Gom các phân loại theo ID sản phẩm để mapper tạo từng dòng response.
        Map<Long, List<ProductVariantEntity>> variantsByProductId =
                productIds.isEmpty()
                        ? Map.of()
                        : productVariantJpa.findByProduct_IdIn(productIds).stream()
                        .collect(Collectors.groupingBy(
                                variant -> variant.getProduct().getId()
                        ));

        List<SellerProductItemResponse> items = productPage.getContent().stream()
                .map(product -> productMapper.toSellerItem(
                        product,
                        variantsByProductId.getOrDefault(product.getId(), List.of())
                ))
                .toList();

        return new PageResponse<>(
                items,
                filter.pageQuery().page(),
                filter.pageQuery().size(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    @Override
    public PageResponse<Product> findProductsFillter(Long sellerUserId, SellerProductFilter filter) {
        Page<ProductEntity> productEntityPage = productJpa.findSellerProducts(sellerUserId,
                filter.categoryId(),
                filter.status() == null ? null : filter.status().name(),
                ProductStatus.ACTIVE,
                ProductStatus.INACTIVE,
                PageRequest.of(
                        filter.pageQuery().pageIndex(),
                        filter.pageQuery().size()
                )
        );

        return  productMapper.toDomainPage(productEntityPage);
    }

}
