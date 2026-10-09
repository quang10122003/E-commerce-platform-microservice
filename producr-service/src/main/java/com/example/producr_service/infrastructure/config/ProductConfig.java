package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.GetShopProductDetailUseCase;
import com.example.producr_service.application.port.in.GetCartInfoUseCase;
import com.example.producr_service.application.port.in.UpdateProductUseCase;
import com.example.producr_service.application.port.in.DeleteProductUseCase;
import com.example.producr_service.application.port.in.DeleteProductvariantUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import com.example.producr_service.application.port.in.GetSellerProductsUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import com.example.producr_service.application.service.FileUploader;
import com.example.producr_service.application.service.ProductCreationService;
import com.example.producr_service.application.service.ShopProductPageAssembler;
import com.example.producr_service.application.service.ProductImageUploadService;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.service.ProductInternalService;
import com.example.producr_service.application.service.ProductUpdateService;
import com.example.producr_service.application.service.ProductHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.common.untill.JsonUtils;
import java.time.Clock;

@Configuration
public class ProductConfig {

    // Cung cấp use case đọc thông tin variant cho API giỏ hàng nội bộ.
    @Bean
    GetCartInfoUseCase getCartInfoUseCase(ProductVariantRepoPort productVariantRepoPort,
                                           ShopInfoClientPort shopInfoClientPort) {
        return new ProductInternalService(productVariantRepoPort, shopInfoClientPort);
    }

    // Đăng ký service chỉ xử lý việc dựng và lưu aggregate khi tạo product.
    @Bean
    ProductCreationService productCreationService(
            ProductRepositoryPort productRepositoryPort,
            ProductHelper productHelper,
            CategoryRepoPort categoryRepositoryPort,
            BrandRepositoryPort brandRepositoryPort
    ) {
        return new ProductCreationService(
                productRepositoryPort,
                productHelper,
                categoryRepositoryPort,
                brandRepositoryPort
        );
    }

    // Đăng ký helper dựng trang sản phẩm từ dữ liệu domain.
    @Bean
    ShopProductPageAssembler sellerProductPageAssembler() {
        return new ShopProductPageAssembler();
    }

    // Đăng ký helper hỗ trợ các xử lý của ProductService.
    @Bean
    ProductHelper productServiceHelper(ProductRepositoryPort productRepositoryPort,
                                       ProductVariantRepoPort productVariantRepoPort) {
        return new ProductHelper(productRepositoryPort, productVariantRepoPort);
    }

    // Đăng ký service xử lý validate, upload và gắn URL ảnh product.
    @Bean
    ProductImageUploadService productImageUploadService(FileUploader fileUploader) {
        return new ProductImageUploadService(fileUploader);
    }

    // Truyền các port đọc, lưu, ảnh và outbox cho luồng chỉnh sửa sản phẩm.
    @Bean
    ProductUpdateService productUpdateService(
            ProductRepositoryPort productRepositoryPort,
            ProductVariantRepoPort productVariantRepoPort,
            CategoryRepoPort categoryRepoPort,
            BrandRepositoryPort brandRepositoryPort,
            CurrentUserPort currentUserPort,
            ProductHelper productHelper,
            FileUploader fileUploader,
            ProductImageRollbackPort productImageRollbackPort,
            OutboxPort outboxPort,
            JsonUtils jsonUtils,
            Clock clock
    ) {
        return new ProductUpdateService(productRepositoryPort, productVariantRepoPort,
                categoryRepoPort, brandRepositoryPort, currentUserPort, productHelper,
                fileUploader, productImageRollbackPort, outboxPort, jsonUtils, clock);
    }

    // Đăng ký application service triển khai các use case thuộc phạm vi product.
    @Bean
    ProductService productService(
        ProductImageRollbackPort productImageRollbackPort,
        ProductImageUploadService productImageUploadService,
        ProductCreationService productCreationService,
        CurrentUserPort currentUserPort,
        ProductSearchIndexPort productSearchIndexPort,
        ProductHelper productHelper,
        OutboxPort outboxPort,
        JsonUtils jsonUtils,
        ShopProductPageAssembler sellerProductPageAssembler,
        ProductRepositoryPort productRepositoryPort,
        ProductVariantRepoPort productVariantRepoPort,
        ProductUpdateService productUpdateService,
        Clock clock
    ) {
        return new ProductService(
                productImageRollbackPort,
                productImageUploadService,
                productCreationService,
                currentUserPort,
                outboxPort,
                productSearchIndexPort,
                productHelper,
                jsonUtils,
                sellerProductPageAssembler,
                productRepositoryPort,
                productVariantRepoPort,
                productUpdateService,
                clock
        );
    }

    // Cung cấp use case tạo product cho controller qua input port.
    @Bean
    CreateProductUseCase createProductUseCase(ProductService productService) {
        return productService;
    }

    // Cho controller đọc dữ liệu sản phẩm qua ProductService, không gọi lớp xử lý phụ.
    @Bean
    GetShopProductDetailUseCase getShopProductDetailUseCase(ProductService productService) {
        return productService;
    }

    // Cho controller cập nhật sản phẩm qua ProductService để giữ transaction chung.
    @Bean
    UpdateProductUseCase updateProductUseCase(ProductService productService) {
        return productService;
    }

    // Cung cấp use case xóa sản phẩm cho controller.
    @Bean
    DeleteProductUseCase deleteProductUseCase(ProductService productService) {
        return productService;
    }

    // Cung cấp use case xóa phân loại cho controller.
    @Bean
    DeleteProductvariantUseCase deleteProductvariantUseCase(ProductService productService) {
        return productService;
    }

    // Cung cấp use case lấy catalog product cho controller qua input port.
    @Bean
    GetProductsCatalogUseCase getProductsCatalogUseCase(ProductService productService) {
        return productService;
    }

    // Cung cấp use case đọc danh sách sản phẩm của người bán cho controller.
    @Bean
    GetSellerProductsUseCase getSellerProductsUseCase(ProductService productService) {
        return productService;
    }
}
