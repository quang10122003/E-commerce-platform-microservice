package com.example.producr_service.adapter.out.persistence.Product;

import com.example.common.response.PageResponse;
import com.example.common.exception.BusinessException;
import com.example.producr_service.adapter.entity.BrandEntity;
import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.adapter.mapper.ProductMapper;
import com.example.producr_service.adapter.out.persistence.Brand.BrandJpa;
import com.example.producr_service.adapter.out.persistence.Category.CategoryJpa;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.model.Product;

import com.example.producr_service.domain.model.ProductStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
// Triển khai port đọc và lưu sản phẩm bằng JPA; chỉ chuyển dữ liệu sang domain.
public class ProductRepoAdapter implements ProductRepositoryPort {
    ProductJpa productJpa;
    CategoryJpa categoryJpa;
    BrandJpa brandJpa;
    ProductMapper productMapper;

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
    // Khóa sản phẩm và lấy ID người sở hữu để kiểm tra quyền xóa.
    @Override
    public Optional<Long> findOwnerIdByIdForUpdate(Long productId) {
        return productJpa.findByIdForLock(productId).map(ProductEntity::getUserId);
    }

    @Override
    public void deleteById(Long productId) {
        productJpa.deleteById(productId);
    }

    // Lấy ảnh bìa của sản phẩm đã khóa mà không tải các quan hệ con.
    @Override
    public StoredFile findCoverFile(Long productId) {
        ProductEntity product = productJpa.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        return new StoredFile(
                product.getStorageBucket(),
                product.getObjectPath(),
                product.getImageUrl()
        );
    }

}
