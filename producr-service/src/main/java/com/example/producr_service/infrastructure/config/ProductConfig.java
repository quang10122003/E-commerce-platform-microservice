package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.out.storage.FileStoragePort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import com.example.producr_service.application.port.in.GetSellerProductsUseCase;
import com.example.producr_service.application.port.in.ProcessProductOutboxUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.ES.ProductSearchPort;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.application.registry.UploadStrategyRegistry;
import com.example.producr_service.application.registry.OutboxEventHandlerRegistry;
import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.adapter.out.openFeign.AuthUserAdapter;
import com.example.producr_service.application.service.FileService;
import com.example.producr_service.application.service.ProductCreationService;
import com.example.producr_service.application.service.ProductHelperService;
import com.example.producr_service.application.service.ProductOutboxStatusService;
import com.example.producr_service.application.service.ProductImageUploadService;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.service.CategoryService;
import com.example.producr_service.application.service.BrandService;
import com.example.producr_service.application.strategy.upload.CategoryImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.ProductImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.ProductVariantImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.IUploadStrategy;
import com.example.producr_service.application.strategy.outbox.OutboxEventHandler;
import com.example.producr_service.application.strategy.outbox.ProductCreatedOutboxHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.common.untill.JsonUtils;

import java.util.List;

@Configuration
public class ProductConfig {

    // Đăng ký adapter lấy thông tin user hiện tại từ Auth service qua Feign.
    @Bean
    CurrentUserPort currentUserPort(AuthUserFeignClient authUserFeignClient) {
        return new AuthUserAdapter(authUserFeignClient);
    }

    // Đăng ký service chỉ xử lý việc dựng và lưu aggregate khi tạo product.
    @Bean
    ProductCreationService productCreationService(
            ProductRepositoryPort productRepositoryPort,
            CategoryRepoPort categoryRepositoryPort,
            BrandRepositoryPort brandRepositoryPort
    ) {
        return new ProductCreationService(
                productRepositoryPort,
                categoryRepositoryPort,
                brandRepositoryPort
        );
    }

    // Đăng ký helper dựng trang sản phẩm từ dữ liệu domain.
    @Bean
    ProductHelperService productHelperService() {
        return new ProductHelperService();
    }

    // Cung cấp service quản lý transaction khi job cập nhật trạng thái outbox.
    @Bean
    ProcessProductOutboxUseCase processProductOutboxUseCase(
            OutboxPort outboxPort,
            OutboxEventHandlerRegistry outboxEventHandlerRegistry
    ) {
        return new ProductOutboxStatusService(outboxPort, outboxEventHandlerRegistry);
    }

    // Đăng ký service xử lý validate, upload và gắn URL ảnh product.
    @Bean
    ProductImageUploadService productImageUploadService(FileService fileService) {
        return new ProductImageUploadService(fileService);
    }

    // Đăng ký service lấy category theo mô hình port in/out của ứng dụng.
    @Bean
    CategoryService categoryService(CategoryRepoPort categoryRepoPort) {
        return new CategoryService(categoryRepoPort);
    }

    // Cung cấp use case lấy category cho adapter vào như controller.
    @Bean
    GetCategoriesUseCase getCategoriesUseCase(CategoryService categoryService) {
        return categoryService;
    }

    // Tạo bean theo port để controller không phụ thuộc implementation của service.
    @Bean
    GetBrandsUseCase getBrandsUseCase(BrandRepositoryPort brandRepositoryPort) {
        return new BrandService(brandRepositoryPort);
    }

    // Đăng ký strategy đồng bộ Product qua các port của application.
    @Bean
    OutboxEventHandler productCreatedOutboxHandler(
            ProductRepositoryPort productRepositoryPort,
            ProductSearchIndexPort productSearchIndexPort,
            MapJsonToObjPort mapJsonToObjPort
    ) {
        return new ProductCreatedOutboxHandler(
                productRepositoryPort,
                productSearchIndexPort,
                mapJsonToObjPort
        );
    }

    // Gom các strategy outbox để định tuyến theo eventType.
    @Bean
    OutboxEventHandlerRegistry outboxEventHandlerRegistry(
            List<OutboxEventHandler> eventHandlers
    ) {
        return new OutboxEventHandlerRegistry(eventHandlers);
    }

    // Đăng ký application service triển khai các use case thuộc phạm vi product.
    @Bean
    ProductService productService(
        ProductImageRollbackPort productImageRollbackPort,
        ProductImageUploadService productImageUploadService,
        ProductCreationService productCreationService,
        CurrentUserPort currentUserPort,
        ProductSearchPort productSearchPort,
        OutboxPort outboxPort,
        JsonUtils jsonUtils,
        ProductHelperService productHelperService,
        ProductRepositoryPort productRepositoryPort,
        ProductVariantRepoPort productVariantRepoPort
    ) {
        return new ProductService(
                productImageRollbackPort,
                productImageUploadService,
                productCreationService,
                currentUserPort,
                outboxPort,
                productSearchPort,
                jsonUtils,
                productHelperService,
                productRepositoryPort,
                productVariantRepoPort
        );
    }

    // Cung cấp use case tạo product cho controller qua input port.
    @Bean
    CreateProductUseCase createProductUseCase(ProductService productService) {
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

    // Đăng ký strategy upload ảnh category trong infrastructure layer.
    @Bean
    IUploadStrategy categoryImageUploadStrategy() {
        return new CategoryImageUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product trong infrastructure layer.
    @Bean
    IUploadStrategy productImageUploadStrategy() {
        return new ProductImageUploadStrategy();
    }

    // Đăng ký strategy upload ảnh product variant đã có trong application layer.
    @Bean
    IUploadStrategy productVariantImageUploadStrategy() {
        return new ProductVariantImageUploadStrategy();
    }

    // Gom các strategy để chọn đúng logic theo loại bucket.
    @Bean
    UploadStrategyRegistry uploadStrategyRegistry(
            List<IUploadStrategy> strategies
    ) {
        return new UploadStrategyRegistry(strategies);
    }

    // Đăng ký application service và cấp adapter lưu trữ cho service.
    @Bean
    FileService fileService(
            FileStoragePort fileStoragePort,
            UploadStrategyRegistry uploadStrategyRegistry
    ) {
        return new FileService(fileStoragePort, uploadStrategyRegistry);
    }
}
