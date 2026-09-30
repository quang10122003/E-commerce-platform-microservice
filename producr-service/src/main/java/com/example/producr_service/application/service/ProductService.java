package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.command.UpdateProductCommand;
import com.example.producr_service.application.dto.outbox.*;
import com.example.producr_service.application.dto.request.CreateProductData;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
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
import java.util.stream.Stream;

// Triển khai các use case nghiệp vụ thuộc phạm vi product.
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductService implements CreateProductUseCase, GetProductsCatalogUseCase , GetSellerProductsUseCase, DeleteProductUseCase, DeleteProductvariantUseCase, GetShopProductDetailUseCase, UpdateProductUseCase {
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
    ProductUpdateService productUpdateService;

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
        return productUpdateService.updateProduct(productId, command);
    }

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


    @Transactional
    @Override
    public void deleteProduct(Long productId) {
        UserInternaInfoRespone userInternaInfoRespone =  currentUserPort.getUserInfo();

        // lấy id user sở hứu sản phẩm và check xem có phải cùng user request k , khóa sản phẩm lock
        productHelper.validateProductOwnership(productId, userInternaInfoRespone.userId());
        // lấy và khóa ProductVariant thuộc product
        List<ProductVariant> variants = productVariantRepoPort.findByProductIdForUpdate(productId);


        // Giữ thông tin ảnh phân loại trước khi xóa bản ghi để dọn storage sau commit.
        List<StoredFile> variantFiles = productVariantRepoPort.findStoredFilesByVariantIds(
                variants.stream().map(ProductVariant::getId).toList()
        );

        // Giữ thông tin ảnh bìa trước khi xóa bản ghi sản phẩm.
        StoredFile coverFile = productRepositoryPort.findCoverFile(productId);

        List<StoredFile> filesToDelete = productHelper.prepareFilesForDeletion(
                productId,
                Stream.concat(Stream.of(coverFile), variantFiles.stream()).toList()
        );
        saveProductDeletionOutboxEvents(productId, filesToDelete);

        // Xóa aggregate và các bản ghi liên quan bằng cascade trong cùng transaction với outbox.
        productRepositoryPort.deleteById(productId);
    }

    // Lưu công việc dọn ảnh và xóa document tìm kiếm trong cùng transaction xóa sản phẩm.
    private void saveProductDeletionOutboxEvents(Long productId, List<StoredFile> filesToDelete) {
        String aggregateId = String.valueOf(productId);
        ProductImagesDeleteOutboxPayload imagesPayload = new ProductImagesDeleteOutboxPayload(filesToDelete);
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.PRODUCT,
                aggregateId,
                ProductImagesDeleteOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(imagesPayload)
        ));

        ProductDeletedOutboxPayload deletedPayload = new ProductDeletedOutboxPayload(productId);
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.PRODUCT,
                aggregateId,
                ProductDeletedOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(deletedPayload)
        ));
    }

    @Transactional
    @Override
    public void deleteProductvariant(Long productId, Long variantId) {
        UserInternaInfoRespone userInternaInfoRespone =  currentUserPort.getUserInfo();
        // Khóa sản phẩm và xác nhận quyền sở hữu trước khi khóa phân loại.
        productHelper.validateProductOwnership(productId, userInternaInfoRespone.userId());
        // Chỉ khóa phân loại thuộc đúng sản phẩm cần xử lý.
        productVariantRepoPort.findByProductIdAndIdForUpdate(productId, variantId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_VARIANT_NOT_FOUND));
        // Đếm sau khi giữ khóa product để chặn hai lần xóa đồng thời phân loại cuối.
        long countVariants = productVariantRepoPort.countByProduct_Id(productId);
        // Giữ lại ít nhất một phân loại cho sản phẩm.
       if(countVariants <=1 ){
           throw  new BusinessException(ProductError.CANNOT_DELETE_LAST_VARIANT);
       }
        // Giữ metadata ảnh trước khi xóa bản ghi phân loại.
        List<StoredFile> variantFiles = productVariantRepoPort.findStoredFilesByVariantIds(List.of(variantId));

        // Kiểm tra metadata và loại ảnh trùng trước khi ghi outbox.
        List<StoredFile> filesToDelete = productHelper.prepareFilesForDeletion(
                productId,
                 variantFiles
        );

        // Lưu tác vụ dọn ảnh cùng transaction xóa phân loại.
        saveProductvariantDeletionOutboxEvents(variantId, filesToDelete,userInternaInfoRespone.shopAddress(),productId);
        productVariantRepoPort.deleteById(variantId);

    }

    private  void saveProductvariantDeletionOutboxEvents(Long variantId, List<StoredFile> filesToDelete,String shopAddress,Long productId){
        String aggregateId = String.valueOf(variantId);
        ProductvariantImagesDeleteOutboxPayload productvariantImagesDeleteOutboxPayload = new ProductvariantImagesDeleteOutboxPayload(filesToDelete);
        ProductvariantDeleteOutboxPayload productvariantDeleteOutboxPayload = new ProductvariantDeleteOutboxPayload(
                productId, productHelper.extractProvinceNameFromAddress(shopAddress)
        );
        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.VARIANT,
                aggregateId,
                ProductvariantImagesDeleteOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(productvariantImagesDeleteOutboxPayload)
        ));

        outboxPort.save(new OutboxEventDto(
                UUID.randomUUID(),
                Constant.VARIANT,
                aggregateId,
                ProductvariantDeleteOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(productvariantDeleteOutboxPayload)
        ));

    }
}
