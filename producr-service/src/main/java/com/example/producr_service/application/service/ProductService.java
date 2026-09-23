package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.outbox.ProductCreatedOutboxPayload;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.ES.ProductSearchPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.example.common.untill.JsonUtils;

import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

// Triển khai các use case nghiệp vụ thuộc phạm vi product.
@RequiredArgsConstructor
public class ProductService implements CreateProductUseCase, GetProductsCatalogUseCase {
    private final FileService fileService;
    private final ProductImageUploadService productImageUploadService;
    private final ProductCreationService productCreationService;
    private final CurrentUserPort currentUserPort;
    private final OutboxPort outboxPort;
    // Port đọc danh sách product từ Elasticsearch.
    private final ProductSearchPort productSearchPort;
    private final JsonUtils jsonUtils;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        CreateProductRequest request = command.productRequest();
        List<VariantImageUploadCommand> variantImages = command.variantImages() == null
                ? List.of()
                : command.variantImages();

        List<StoredFile> storedFiles = new java.util.ArrayList<>();

        try {
            // Validate, upload ảnh và gắn URL vào request trước khi tạo product.
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
        } catch (RuntimeException exception) {
            try {
                fileService.deleteStoredFiles(storedFiles);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    // Tạo event outbox tối đẩy lên db
    private void saveProductCreatedOutboxEvent(Product product, UserInternaInfoRespone userInternaInfoRespone ) {

        // lấy tên tỉnh của shop tạo sản phẩm
        String shopAddress = userInternaInfoRespone.shopAddress();
        String locationProduct = shopAddress == null || shopAddress.isBlank()
                ? null
                : shopAddress.substring(shopAddress.lastIndexOf(",") + 1).trim();
        ProductCreatedOutboxPayload payload = new ProductCreatedOutboxPayload(product.getId(),locationProduct);
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                "Product",
                String.valueOf(product.getId()),
                ProductCreatedOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(payload)
        ));
    }

    @Override
    public ProductCatalogSearchResponse getProducts(ProductScrollFilter filter) {
        validateSearchKeyword(filter.keyword());
        validatePriceRange(filter.minPrice(), filter.maxPrice());
        return productSearchPort.search(filter);
    }

    // Chặn truy vấn catalog khi client không truyền từ khóa tìm kiếm.
    private void validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(
                    ProductError.SEARCH_KEYWORD_REQUIRED,
                    "keyword khong duoc de trong"
            );
        }
    }

    // Kiểm tra khoảng giá trước khi gửi query tìm kiếm sang Elasticsearch.
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(
                    ProductError.INVALID_PRICE_RANGE,
                    "minPrice khong duoc lon hon maxPrice"
            );
        }
    }
}
