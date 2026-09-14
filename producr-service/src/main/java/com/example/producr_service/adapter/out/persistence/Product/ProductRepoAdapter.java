package com.example.producr_service.adapter.out.persistence.Product;

import com.example.producr_service.adapter.entity.BrandEntity;
import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.adapter.mapper.ProductMapper;
import com.example.producr_service.adapter.out.persistence.Brand.BrandJpa;
import com.example.producr_service.adapter.out.persistence.Category.CategoryJpa;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.port.out.ProductRepositoryPort;
import com.example.producr_service.domain.model.Product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProductRepoAdapter implements ProductRepositoryPort {
    ProductJpa productJpa;
    ProductVariantJpa productVariantJpa;
    CategoryJpa categoryJpa;
    BrandJpa brandJpa;
    ProductMapper productMapper;

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
    public Optional<Product> findById(Long id) {
        return productJpa.findById(id).map(productMapper::toDomain);
    }

    // check sku tồn tại chưa
    @Override
    public boolean existsBySku(String sku) {
        return productVariantJpa.existsBySku(sku);
    }
}
