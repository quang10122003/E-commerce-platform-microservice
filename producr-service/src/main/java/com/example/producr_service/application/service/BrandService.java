package com.example.producr_service.application.service;

import lombok.extern.slf4j.Slf4j;
import com.example.common.untill.ValidationUtils;
import com.example.producr_service.application.port.in.DeleteBrandUseCase;
import com.example.producr_service.application.port.in.CreateBrandUseCase;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import java.time.Clock;
import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.GetAdminBrandsUseCase;
import com.example.producr_service.domain.model.Brand;
import com.example.producr_service.domain.error.DomainProductError;
import com.example.producr_service.application.dto.response.BrandResponse;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class BrandService implements GetBrandsUseCase, GetAdminBrandsUseCase, DeleteBrandUseCase, CreateBrandUseCase {
    private final BrandRepositoryPort brandRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final OutboxPort outboxPort;
    private final Clock clock;

    @Transactional
    @Override
    public BrandResponse createBrand(String name) {
        String brandName = ValidationUtils.requireNormalized(name, DomainProductError.BRAND_NAME_REQUIRED);
        if (brandRepositoryPort.existsByName(brandName)) {
            throw new BusinessException(ProductError.BRAND_NAME_DUPLICATED);
        }
        Brand brand = brandRepositoryPort.save(new Brand(null, brandName));
        log.info("Tạo thương hiệu thành công brandId={}", brand.getId());
        return BrandResponse.from(brand);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BrandResponse> getBrands() {
        List<Brand> brands = brandRepositoryPort.getAllBrands();
        Map<Long, Long> productCounts = productRepositoryPort.countProductsByBrandIds(
                brands.stream().map(Brand::getId).toList());
        return brands
                .stream()
                .map(brand -> BrandResponse.from(brand, productCounts.getOrDefault(brand.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<BrandResponse> getAdminBrands(int page, int size, String keyword) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(ProductError.INVALID_PAGINATION);
        }
        PageResponse<Brand> brands = brandRepositoryPort.findAdminBrands(
                new PageQuery(page, size), keyword == null || keyword.isBlank() ? null : keyword.trim());
        Map<Long, Long> productCounts = productRepositoryPort.countProductsByBrandIds(
                brands.items().stream().map(Brand::getId).toList());
        return new PageResponse<>(brands.items().stream()
                .map(brand -> BrandResponse.from(brand, productCounts.getOrDefault(brand.getId(), 0L))).toList(),
                brands.page(), brands.pageSize(), brands.totalItems(), brands.totalPages());
    }

    // FK products.brands_id ON DELETE SET NULL tự gỡ thương hiệu khỏi sản phẩm; chỉ cần đồng bộ lại ES và embedding.
    @Transactional
    @Override
    public void deleteBrand(Long brandId) {
        if (!brandRepositoryPort.existsById(brandId)) {
            throw new BusinessException(ProductError.BRAND_NOT_FOUND);
        }
        // Lấy danh sách trước khi xóa vì sau đó brands_id của các sản phẩm đã thành NULL.
        List<Long> productIds = productRepositoryPort.findIdsByBrandId(brandId);
        brandRepositoryPort.deleteById(brandId);
        productIds.forEach(productId -> ProductSyncEvents.save(outboxPort, clock, productId));
        log.info("Xóa thương hiệu thành công brandId={}, productCount={}", brandId, productIds.size());
    }
}
