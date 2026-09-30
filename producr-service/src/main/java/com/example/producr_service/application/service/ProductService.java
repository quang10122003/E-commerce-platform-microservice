package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.outbox.*;
import com.example.producr_service.application.dto.request.CreateProductData;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.*;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductVariant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import com.example.common.untill.JsonUtils;

import java.util.*;

// Triển khai các use case nghiệp vụ thuộc phạm vi product.
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductService implements CreateProductUseCase, GetProductsCatalogUseCase , GetSellerProductsUseCase {
    ProductImageRollbackPort productImageRollbackPort;
    ProductImageUploadService productImageUploadService;
    ProductCreationService productCreationService;
    CurrentUserPort currentUserPort;
    OutboxPort outboxPort;
    // Port thao tác chỉ mục và tìm kiếm product trên Elasticsearch.
    ProductSearchIndexPort productSearchIndexPort;
    ProductHelper productHelper;
    JsonUtils jsonUtils;
    ShopProductPageAssembler sellerProductPageAssembler;
    ProductRepositoryPort productRepositoryPort;
    ProductVariantRepoPort productVariantRepoPort;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        CreateProductData request = command.productRequest();
        List<VariantImageUploadCommand> variantImages = command.variantImages() == null
                ? List.of()
                : command.variantImages();

        List<StoredFile> storedFiles = new ArrayList<>();
        // Đăng ký trước khi upload để cả lỗi upload dở dang cũng được dọn sau rollback.
        productImageRollbackPort.deleteImageStore(storedFiles);

        // Validate, upload ảnh và gắn metadata vào request trước khi tạo product.
        productImageUploadService.uploadAndApplyImageUrls(
                request,
                command.productImage(),
                variantImages,
                storedFiles
        );

        UserInternaInfoRespone userInternaInfoRespone = currentUserPort.getUserInfo();
        Product product = productCreationService.createProduct(userInternaInfoRespone, request);

        // lưu outbox
        saveProductCreatedOutboxEvent(product,userInternaInfoRespone);

        return ProductResponse.from(product);
    }

    // Tạo event outbox tối đẩy lên db
    private void saveProductCreatedOutboxEvent(Product product, UserInternaInfoRespone userInternaInfoRespone ) {

        // lấy tên tỉnh của shop tạo sản phẩm
        String shopAddress = userInternaInfoRespone.shopAddress();
        String locationProduct =  productHelper.extractProvinceNameFromAddress(shopAddress);
        ProductCreatedOutboxPayload payload = new ProductCreatedOutboxPayload(product.getId(),locationProduct);
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.PRODUCT,
                String.valueOf(product.getId()),
                ProductCreatedOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(payload)
        ));
    }

    @Override
    public ProductCatalogSearchResponse getProducts(ProductScrollFilter filter) {
        productHelper.validateSearchKeyword(filter.keyword());
        productHelper.validatePriceRange(filter.minPrice(), filter.maxPrice());
        return productSearchIndexPort.search(filter);
    }

    // Lấy danh sách sản phẩm có phân trang cho shop.
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerProductItemResponse> getSellerProducts(SellerProductFilter filter) {
        // Gọi auth-service lấy ID người bán từ phiên đăng nhập.
        Long userId = currentUserPort.getUserInfo().userId();
        PageResponse<Product> pageProduct = productRepositoryPort.findProductsFillter(userId,filter);

        List<Product> products = pageProduct.items();
        // Lấy ID trang hiện tại để truy vấn thuộc tính phân loại theo lô, tránh N+1.
        List<Long> productIds = sellerProductPageAssembler.getListIdProduct(products);

        Map<Long, List<ProductVariant>> variantsByProductId =
                productIds.isEmpty()
                        ? Map.of()
                        : productVariantRepoPort.findByProduct_IdIn(productIds);

        return sellerProductPageAssembler.buildSellerProductPage(pageProduct, variantsByProductId);
    }



}
