package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResult;
import com.example.producr_service.application.dto.response.ProductSaleOfferView;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.dto.response.ProductBrandOption;
import com.example.producr_service.application.dto.response.VariantSaleResponse;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.command.UpdateProductCommand;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.outbox.*;
import com.example.producr_service.application.dto.request.CreateProductData;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.request.ShopProductFilter;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.ProductPriceRangeResponse;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.dto.response.ShopFlashSaleProductResponse;
import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.dto.response.UserProductDetailResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.mapper.UserProductDetailMapper;
import com.example.producr_service.application.port.in.*;
import com.example.producr_service.application.port.in.GetShopFlashSaleProductsUseCase;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.model.Brand;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductVariant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;

// Triển khai các use case nghiệp vụ thuộc phạm vi product.
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductService implements CreateProductUseCase, GetProductsCatalogUseCase , GetSellerProductsUseCase, DeleteProductUseCase, DeleteProductvariantUseCase, GetShopProductDetailUseCase, UpdateProductUseCase, GetShopFlashSaleProductsUseCase, GetUserProductDetailUseCase, GetProductPriceRangeUseCase {
    ProductImageRollbackPort productImageRollbackPort;
    ProductImageUploadService productImageUploadService;
    ProductCreationService productCreationService;
    CurrentUserPort currentUserPort;
    OutboxPort outboxPort;
    // Port thao tác chỉ mục và tìm kiếm product trên Elasticsearch.
    ProductSearchIndexPort productSearchIndexPort;
    ProductHelper productHelper;
    ShopProductPageAssembler sellerProductPageAssembler;
    ProductRepositoryPort productRepositoryPort;
    ProductVariantRepoPort productVariantRepoPort;
    ProductUpdateService productUpdateService;
    Clock clock;
    ShopInfoClientPort shopInfoClientPort;
    BrandRepositoryPort brandRepositoryPort;
    FlashSaleItemRepositoryPort flashSaleItemRepositoryPort;
    VariantSaleResolver variantSaleResolver;

    @Override
    @Transactional(readOnly = true)
    public ProductPriceRangeResponse getProductPriceRange(Long productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        Product.PriceRange priceRange = product.getPriceRange();
        return new ProductPriceRangeResponse(product.getId(), priceRange.getMin().getAmount(), priceRange.getMax().getAmount());
    }

    // Giữ phiên đọc cho ảnh và thuộc tính; trả cả trạng thái product để FE xử lý hiển thị.
    @Override
    @Transactional(readOnly = true)
    public UserProductDetailResponse getUserProductDetail(Long productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        String shopAddress = shopInfoClientPort.findPublicShopAddress(product.getUserId()).orElse(null);
        String location = productHelper.extractProvinceNameFromAddress(shopAddress);
        return UserProductDetailMapper.toResponse(product, location);
    }

    // Chỉ đọc sản phẩm của Shop đang đăng nhập, không nhận shopId từ client.
    @Override
    @Transactional(readOnly = true)
    public List<ShopFlashSaleProductResponse> getShopFlashSaleProducts() {
        Long userId = currentUserPort.getUserInfo().userId();
        List<Product> products = productRepositoryPort.findAllByUserId(userId);
        List<Long> productIds = sellerProductPageAssembler.getListIdProduct(products);
        // Tái sử dụng truy vấn phân loại theo lô của API quản lý sản phẩm.
        Map<Long, List<ProductVariant>> variantsByProductId = productIds.isEmpty()
                ? Map.of() : productVariantRepoPort.findByProduct_IdIn(productIds);
        return sellerProductPageAssembler.buildFlashSaleProducts(products, variantsByProductId);
    }

    // Giữ phiên đọc để lấy đủ thuộc tính, phân loại và ảnh cho form chỉnh sửa.
    @Override
    @Transactional(readOnly = true)
    public ShopProductDetailResponse getShopProductDetail(Long productId) {
        return productUpdateService.getShopProductDetail(productId);
    }

    // Gộp thay đổi sản phẩm và outbox trong một transaction để không lưu dở dang.
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopProductDetailResponse updateProduct(Long productId, UpdateProductCommand command) {
        log.info("Bắt đầu cập nhật sản phẩm productId={}", productId);
        try {
            ShopProductDetailResponse response = productUpdateService.updateProduct(productId, command);
            log.info("Cập nhật sản phẩm thành công productId={}", productId);
            return response;
        } catch (RuntimeException exception) {
            log.error("Cập nhật sản phẩm thất bại productId={}", productId, exception);
            throw exception;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        log.info("Bắt đầu tạo sản phẩm có phân loại");
        try {
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

            log.info("Tạo sản phẩm thành công productId={}, variantCount={}",
                    product.getId(), product.getVariants().size());
            return ProductResponse.from(product);
        } catch (RuntimeException exception) {
            log.error("Tạo sản phẩm thất bại", exception);
            throw exception;
        }
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
                payload,
                LocalDateTime.now(clock)
        ));
    }

    @Override
    public ProductCatalogSearchResponse getProducts(ProductScrollFilter filter) {
        // Cho phép tìm độc lập theo danh mục; ngoài ra vẫn yêu cầu từ khóa.
        if (filter.categoryId() == null || filter.categoryId() <= 0) {
            productHelper.validateSearchKeyword(filter.keyword());
        }
        productHelper.validatePriceRange(filter.minPrice(), filter.maxPrice());
        ProductCatalogSearchResult result = productSearchIndexPort.search(filter);
        List<ProductSearchResponse> items = applyCatalogDiscounts(result.items());
        Map<Long, ProductBrandOption> brandsById = brandRepositoryPort.findAllById(result.brandIds()).stream()
                .collect(Collectors.toMap(Brand::getId,
                        brand -> new ProductBrandOption(brand.getId(), brand.getName())));
        List<ProductBrandOption> brands = result.brandIds().stream()
                .map(brandsById::get)
                .filter(Objects::nonNull)
                .toList();
        return new ProductCatalogSearchResponse(
                items,
                result.nextCursor(),
                result.hasNext(),
                brands
        );
    }

    public PageResponse<ProductSearchResponse> getShopProducts(Long shopId, ShopProductFilter filter) {
        PageResponse<ProductSearchResponse> result = productSearchIndexPort.searchShopProducts(shopId, filter);
        return new PageResponse<>(applyCatalogDiscounts(result.items()), result.page(), result.pageSize(),
                result.totalItems(), result.totalPages());
    }

    private List<ProductSearchResponse> applyCatalogDiscounts(List<ProductSearchResponse> items) {
        List<Long> productIds = items.stream().map(ProductSearchResponse::id).distinct().toList();
        Map<Long, BigDecimal> discounts = findCatalogDiscounts(productIds, LocalDateTime.now(clock));
        return items.stream().map(item -> item.withMaxDiscountPercent(discounts.get(item.id()))).toList();
    }

//    lấy phần trắm sale cao nhất của các variant trong product
    private Map<Long, BigDecimal> findCatalogDiscounts(List<Long> productIds, LocalDateTime now) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        // lấy các sale của các variant
        Map<Long, List<ProductSaleOfferView>> offersByVariant = flashSaleItemRepositoryPort
                .findOngoingOffersByProductIds(productIds, now).stream()
                .collect(Collectors.groupingBy(ProductSaleOfferView::variantId));
        Map<Long, BigDecimal> discounts = new HashMap<>();

        for (List<ProductSaleOfferView> offers : offersByVariant.values()) {
            ProductSaleOfferView variant = offers.getFirst();
            // Dùng chung quy tắc với trang chi tiết để không quảng bá sale hết quota hoặc tồn kho.
            VariantSaleResponse.Sale sale = variantSaleResolver.resolve(
                    variant.price(), variant.stockQuantity(),
                    offers.stream().map(ProductSaleOfferView::toOffer).toList());
            if (sale != null) {
                // lưu sale % cao nhất của 1 prodcut
                discounts.merge(variant.productId(), sale.discountPercent(), BigDecimal::max);
            }
        }
        return discounts;
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


    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        log.info("Bắt đầu xóa sản phẩm productId={}", productId);
        try {
            UserInternaInfoRespone userInternaInfoRespone = currentUserPort.getUserInfo();

            // lấy id user sở hứu sản phẩm và check xem có phải cùng user request k , khóa sản phẩm lock
            productHelper.validateProductOwnership(productId, userInternaInfoRespone.userId());
            // lấy và khóa ProductVariant thuộc product
            List<ProductVariant> variants = productVariantRepoPort.findByProductIdForUpdate(productId);
            // Giữ ID các variant trước khi cascade xóa để đưa vào event outbox.
            List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
            // Giữ thông tin ảnh phân loại trước khi xóa bản ghi để dọn storage sau commit.
            List<StoredFile> variantFiles = productVariantRepoPort.findStoredFilesByVariantIds(
                    variantIds
            );

            // Giữ thông tin ảnh bìa trước khi xóa bản ghi sản phẩm.
            StoredFile coverFile = productRepositoryPort.findCoverFile(productId);

            List<StoredFile> filesToDelete = productHelper.prepareFilesForDeletion(
                    productId,
                    Stream.concat(Stream.of(coverFile), variantFiles.stream()).toList()
            );
            saveProductDeletionOutboxEvents(productId, variantIds, filesToDelete);

            // Xóa aggregate và các bản ghi liên quan bằng cascade trong cùng transaction với outbox.
            productRepositoryPort.deleteById(productId);
            log.info("Xóa sản phẩm thành công productId={}", productId);
        } catch (RuntimeException exception) {
            log.error("Xóa sản phẩm thất bại productId={}", productId, exception);
            throw exception;
        }
    }

    // Lưu công việc dọn ảnh và xóa document tìm kiếm, kèm ID variant trong cùng transaction xóa sản phẩm.
    private void saveProductDeletionOutboxEvents(Long productId, List<Long> variantIds,
                                                List<StoredFile> filesToDelete) {
        String aggregateId = String.valueOf(productId);
        ProductImagesDeleteOutboxPayload imagesPayload = new ProductImagesDeleteOutboxPayload(filesToDelete);
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.PRODUCT,
                aggregateId,
                ProductImagesDeleteOutboxPayload.EVENT_TYPE,
                imagesPayload,
                LocalDateTime.now(clock)
        ));
        UUID deletedEventId = UUID.randomUUID();
        LocalDateTime timeDeletedEventId =  LocalDateTime.now(clock);
        ProductDeletedOutboxPayload deletedPayload = new ProductDeletedOutboxPayload(deletedEventId,ProductDeletedOutboxPayload.EVENT_TYPE,productId, variantIds,timeDeletedEventId);
        outboxPort.save(new OutboxEventDto(
                deletedEventId,
                Constant.PRODUCT,
                aggregateId,
                ProductDeletedOutboxPayload.EVENT_TYPE,
                deletedPayload,
                timeDeletedEventId
        ));
    }

    @Transactional
    @Override
    public void deleteProductvariant(Long productId, Long variantId) {
        log.info("Bắt đầu xóa phân loại productId={}, variantId={}", productId, variantId);
        try {
            UserInternaInfoRespone userInternaInfoRespone = currentUserPort.getUserInfo();
            // Khóa sản phẩm và xác nhận quyền sở hữu trước khi khóa phân loại.
            productHelper.validateProductOwnership(productId, userInternaInfoRespone.userId());
            // Chỉ khóa phân loại thuộc đúng sản phẩm cần xử lý.
            productVariantRepoPort.findByProductIdAndIdForUpdate(productId, variantId)
                    .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND));
            // Đếm sau khi giữ khóa product để chặn hai lần xóa đồng thời phân loại cuối.
            long countVariants = productVariantRepoPort.countByProduct_Id(productId);
            // Giữ lại ít nhất một phân loại cho sản phẩm.
            if (countVariants <= 1) {
                throw new BusinessException(ProductError.CANNOT_DELETE_LAST_VARIANT);
            }
            // Giữ metadata ảnh trước khi xóa bản ghi phân loại.
            List<StoredFile> variantFiles = productVariantRepoPort.findStoredFilesByVariantIds(List.of(variantId));

            // Kiểm tra metadata và loại ảnh trùng trước khi ghi outbox.
            List<StoredFile> filesToDelete = productHelper.prepareFilesForDeletion(
                    productId,
                    variantFiles
            );

            // Lưu tác vụ dọn ảnh cùng transaction xóa phân loại.
            saveProductvariantDeletionOutboxEvents(
                    variantId, filesToDelete, userInternaInfoRespone.shopAddress(), productId);
            productVariantRepoPort.deleteById(variantId);
            log.info("Xóa phân loại thành công productId={}, variantId={}", productId, variantId);
        } catch (RuntimeException exception) {
            log.error("Xóa phân loại thất bại productId={}, variantId={}", productId, variantId, exception);
            throw exception;
        }
    }

    // Lưu tác vụ dọn ảnh và event xóa variant với metadata khớp payload tương ứng.
    private void saveProductvariantDeletionOutboxEvents(Long variantId, List<StoredFile> filesToDelete, String shopAddress, Long productId) {
        String aggregateId = String.valueOf(variantId);
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(clock);
        ProductvariantImagesDeleteOutboxPayload productvariantImagesDeleteOutboxPayload = new ProductvariantImagesDeleteOutboxPayload(filesToDelete);
        ProductvariantDeleteOutboxPayload productvariantDeleteOutboxPayload = new ProductvariantDeleteOutboxPayload(
                eventId, ProductvariantDeleteOutboxPayload.EVENT_TYPE,
                productId, variantId, productHelper.extractProvinceNameFromAddress(shopAddress), now
        );
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.VARIANT,
                aggregateId,
                ProductvariantImagesDeleteOutboxPayload.EVENT_TYPE,
                productvariantImagesDeleteOutboxPayload,
                now
        ));

        outboxPort.save(new OutboxEventDto(
                eventId,
                Constant.VARIANT,
                aggregateId,
                ProductvariantDeleteOutboxPayload.EVENT_TYPE,
                productvariantDeleteOutboxPayload,
                now
        ));

    }
}
