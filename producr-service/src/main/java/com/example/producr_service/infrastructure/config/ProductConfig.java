package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.port.out.storage.FileStoragePort;
import com.example.producr_service.application.port.out.ShopProductDetailMapperPort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.GetShopProductDetailUseCase;
import com.example.producr_service.application.port.in.UpdateProductUseCase;
import com.example.producr_service.application.port.in.DeleteProductUseCase;
import com.example.producr_service.application.port.in.DeleteProductvariantUseCase;
import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import com.example.producr_service.application.port.in.GetSellerProductsUseCase;
import com.example.producr_service.application.port.in.ProcessProductOutboxUseCase;
import com.example.producr_service.application.port.in.DeleteStoredFilesUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.application.registry.UploadStrategyRegistry;
import com.example.producr_service.application.registry.OutboxEventHandlerRegistry;
import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.adapter.out.openFeign.AuthUserAdapter;
import com.example.producr_service.application.service.FileUploader;
import com.example.producr_service.application.service.StoredFileCleaner;
import com.example.producr_service.application.service.DeleteStoredFilesService;
import com.example.producr_service.application.service.ProductCreationService;
import com.example.producr_service.application.service.ShopProductPageAssembler;
import com.example.producr_service.application.service.ProductOutboxStatusService;
import com.example.producr_service.application.service.ProductImageUploadService;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.service.ProductUpdateService;
import com.example.producr_service.application.service.ProductHelper;
import com.example.producr_service.application.service.CategoryService;
import com.example.producr_service.application.service.BrandService;
import com.example.producr_service.application.strategy.upload.CategoryImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.ProductImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.ProductVariantImageUploadStrategy;
import com.example.producr_service.application.strategy.upload.IUploadStrategy;
import com.example.producr_service.application.strategy.outbox.OutboxEventHandler;
import com.example.producr_service.application.strategy.outbox.ProductCreatedOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductUpdatedOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductImagesDeleteOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductvariantImagesDeleteOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductDeletedOutboxHandler;
import com.example.producr_service.application.strategy.outbox.ProductvariantDeleteOutboxHandler;
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
    ProductImageUploadService productImageUploadService(FileUploader fileUploader) {
        return new ProductImageUploadService(fileUploader);
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

    // Đăng ký handler đọc lại sản phẩm đã lưu để cập nhật chỉ mục tìm kiếm.
    @Bean
    OutboxEventHandler productUpdatedOutboxHandler(
            ProductRepositoryPort productRepositoryPort,
            ProductSearchIndexPort productSearchIndexPort,
            MapJsonToObjPort mapJsonToObjPort
    ) {
        return new ProductUpdatedOutboxHandler(
                productRepositoryPort, productSearchIndexPort, mapJsonToObjPort);
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
            ShopProductDetailMapperPort shopProductDetailMapperPort
    ) {
        return new ProductUpdateService(productRepositoryPort, productVariantRepoPort,
                categoryRepoPort, brandRepositoryPort, currentUserPort, productHelper,
                fileUploader, productImageRollbackPort, outboxPort, jsonUtils,
                shopProductDetailMapperPort);
    }

    // Đăng ký handler dọn ảnh sản phẩm sau khi event xóa được xử lý.
    @Bean
    OutboxEventHandler productImagesDeleteOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            StoredFileCleaner storedFileCleaner
    ) {
        return new ProductImagesDeleteOutboxHandler(mapJsonToObjPort, storedFileCleaner);
    }

    // Đăng ký handler dọn ảnh phân loại sau khi transaction xóa commit.
    @Bean
    OutboxEventHandler productvariantImagesDeleteOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            StoredFileCleaner storedFileCleaner
    ) {
        return new ProductvariantImagesDeleteOutboxHandler(mapJsonToObjPort, storedFileCleaner);
    }

    // Đăng ký handler cập nhật chỉ mục sau khi xóa phân loại.
    @Bean
    OutboxEventHandler productvariantDeleteOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            ProductSearchIndexPort productSearchIndexPort,
            ProductRepositoryPort productRepositoryPort
    ) {
        return new ProductvariantDeleteOutboxHandler(
                mapJsonToObjPort, productSearchIndexPort, productRepositoryPort
        );
    }

    // Đăng ký handler xóa document tìm kiếm sau khi sản phẩm đã bị xóa.
    @Bean
    OutboxEventHandler productDeletedOutboxHandler(
            MapJsonToObjPort mapJsonToObjPort,
            ProductSearchIndexPort productSearchIndexPort
    ) {
        return new ProductDeletedOutboxHandler(mapJsonToObjPort, productSearchIndexPort);
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
        ProductSearchIndexPort productSearchIndexPort,
        ProductHelper productHelper,
        OutboxPort outboxPort,
        JsonUtils jsonUtils,
        ShopProductPageAssembler sellerProductPageAssembler,
        ProductRepositoryPort productRepositoryPort,
        ProductVariantRepoPort productVariantRepoPort,
        ProductUpdateService productUpdateService
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
                productUpdateService
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

    // Dùng chung logic dọn file cho rollback, upload lỗi và outbox.
    @Bean
    StoredFileCleaner storedFileCleaner(FileStoragePort fileStoragePort) {
        return new StoredFileCleaner(fileStoragePort);
    }

    // Cung cấp use case dọn file cho listener sau rollback.
    @Bean
    DeleteStoredFilesUseCase deleteStoredFilesUseCase(StoredFileCleaner storedFileCleaner) {
        return new DeleteStoredFilesService(storedFileCleaner);
    }

    // Cung cấp component upload file cho xử lý nội bộ.
    @Bean
    FileUploader fileUploader(
            FileStoragePort fileStoragePort,
            UploadStrategyRegistry uploadStrategyRegistry,
            StoredFileCleaner storedFileCleaner
    ) {
        return new FileUploader(fileStoragePort, uploadStrategyRegistry, storedFileCleaner);
    }
}
