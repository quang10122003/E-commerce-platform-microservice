package com.example.producr_service.adapter.in.rest;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.mapper.ProductMultipartCommandMapper;
import com.example.producr_service.adapter.DTO.request.CreateProductRequest;
import com.example.producr_service.adapter.DTO.request.UpdateProductRequest;
import com.example.producr_service.adapter.DTO.request.VariantImageMeta;
import com.example.producr_service.application.dto.request.*;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.ProductPriceRangeResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.DeleteProductUseCase;
import com.example.producr_service.application.port.in.DeleteProductvariantUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import com.example.producr_service.application.port.in.GetProductPriceRangeUseCase;
import com.example.producr_service.application.port.in.GetSellerProductsUseCase;
import com.example.producr_service.application.port.in.GetShopProductDetailUseCase;
import com.example.producr_service.application.port.in.UpdateProductUseCase;
import com.example.producr_service.application.port.in.GetUserProductDetailUseCase;
import com.example.producr_service.application.port.in.GetVariantSaleUseCase;
import com.example.producr_service.application.dto.response.UserProductDetailResponse;
import com.example.producr_service.application.dto.response.VariantSaleResponse;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {

    CreateProductUseCase createProductUseCase;
    GetProductsCatalogUseCase getProductsCatalogUseCase;
    ProductMultipartCommandMapper productMultipartCommandMapper;
    GetSellerProductsUseCase getSellerProductsUseCase;
    DeleteProductUseCase deleteProductUseCase;
    DeleteProductvariantUseCase deleteProductvariantUseCase;
    GetShopProductDetailUseCase getShopProductDetailUseCase;
    UpdateProductUseCase updateProductUseCase;
    GetUserProductDetailUseCase getUserProductDetailUseCase;
    GetVariantSaleUseCase getVariantSaleUseCase;
    GetProductPriceRangeUseCase getProductPriceRangeUseCase;

    @GetMapping("/{productId}/price-range")
    public ResponseEntity<ProductPriceRangeResponse> getProductPriceRange(
            @PathVariable("productId") @Positive Long productId) {
        return ResponseEntity.ok(getProductPriceRangeUseCase.getProductPriceRange(productId));
    }

    // Đọc chi tiết product công khai cùng trạng thái và tổ hợp variant.
    @GetMapping("/{productId}")
    public ResponseEntity<UserProductDetailResponse> getUserProductDetail(
            @PathVariable("productId") @Positive Long productId) {
        return ResponseEntity.ok(getUserProductDetailUseCase.getUserProductDetail(productId));
    }

    // Chỉ trả ưu đãi sale hiện áp dụng cho variant đang chọn.
    @GetMapping("/{productId}/variants/{variantId}/sale")
    public ResponseEntity<VariantSaleResponse> getVariantSale(
            @PathVariable("productId") @Positive Long productId,
            @PathVariable("variantId") @Positive Long variantId) {
        return ResponseEntity.ok(getVariantSaleUseCase.getVariantSale(productId, variantId));
    }


    //  api tạo sản phẩm của shop
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("request") @Valid CreateProductRequest request,
            @RequestPart("productImage") MultipartFile productImage,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages,
            @RequestPart(value = "variantImageMeta", required = false) @Valid List<VariantImageMeta> variantImageMeta
    ) throws IOException {
        int variantCount = request.getVariants() == null ? 0 : request.getVariants().size();
        int variantImageCount = variantImages == null ? 0 : variantImages.size();
        log.info("Nhận yêu cầu tạo sản phẩm variantCount={}, variantImageCount={}",
                variantCount, variantImageCount);
        ProductResponse response = createProductUseCase.createProduct(
                productMultipartCommandMapper.toCommand(
                        request,
                        productImage,
                        variantImages,
                        variantImageMeta
                )
        );
        log.info("Đã tạo sản phẩm productId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Lấy đủ thuộc tính, phân loại và ảnh để người bán mở form chỉnh sửa.
    @GetMapping("/shop/{productId}")
    public ResponseEntity<ShopProductDetailResponse> getShopProductDetail(
            @PathVariable Long productId) {
        return ResponseEntity.ok(getShopProductDetailUseCase.getShopProductDetail(productId));
    }

    // Nhận dữ liệu form cùng ảnh mới để cập nhật sản phẩm trong một lần lưu.
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShopProductDetailResponse> updateProduct(
            @PathVariable Long productId,
            @RequestPart("request") @Valid UpdateProductRequest request,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages,
            @RequestPart(value = "variantImageMeta", required = false) @Valid List<VariantImageMeta> variantImageMeta
    ) throws IOException {
        int variantCount = request.getVariants() == null ? 0 : request.getVariants().size();
        int variantImageCount = variantImages == null ? 0 : variantImages.size();
        log.info("Nhận yêu cầu cập nhật sản phẩm productId={}, variantCount={}, variantImageCount={}",
                productId, variantCount, variantImageCount);
        ShopProductDetailResponse response = updateProductUseCase.updateProduct(productId,
                productMultipartCommandMapper.toUpdateCommand(
                        request, productImage, variantImages, variantImageMeta));
        log.info("Đã cập nhật sản phẩm productId={}", productId);
        return ResponseEntity.ok(response);
    }


    // API lấy catalog product đang hoạt động bằng cơ chế infinity scroll.
    @GetMapping()
    public ResponseEntity<ProductCatalogSearchResponse> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(required = false) @DecimalMin("0") BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin("0") BigDecimal maxPrice,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) List<String> locations,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(name = "sort", defaultValue = "RELEVANCE") ProductSortOption sortOption
    ) {
        ProductScrollFilter filter = new ProductScrollFilter(
                keyword,
                categoryId,
                brandIds,
                minPrice,
                maxPrice,
                cursor,
                locations,
                size,
                sortOption
        );

        return ResponseEntity.ok(getProductsCatalogUseCase.getProducts(filter));
    }

    // api lấy danh sach sản phẩm  và các biến thể liên của shop có phân trang
    @GetMapping("shop")
    ResponseEntity<PageResponse<SellerProductItemResponse>> getSellerProductsUseCase(@RequestParam(required = false) Long categoryId, @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size, @RequestParam int page,@RequestParam(required = false) SellerProductFilter.Status status,@RequestParam(required = false) String keyword){
        SellerProductFilter sellerProductFilter = new SellerProductFilter(
                PageQuery.builder().page(page).size(size).build(), categoryId, status, keyword);
        return ResponseEntity.ok(getSellerProductsUseCase.getSellerProducts(sellerProductFilter));
    }

    // Xóa sản phẩm của shop và ghi tác vụ dọn ảnh, chỉ mục vào outbox.
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        log.info("Nhận yêu cầu xóa sản phẩm productId={}", productId);
        deleteProductUseCase.deleteProduct(productId);
        log.info("Đã xử lý yêu cầu xóa sản phẩm productId={}", productId);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    public  ResponseEntity<Void> deleteProductvariant(@PathVariable("productId") Long productId,@PathVariable("variantId") Long variantsId){
        log.info("Nhận yêu cầu xóa phân loại productId={}, variantId={}", productId, variantsId);
        deleteProductvariantUseCase.deleteProductvariant(productId, variantsId);
        log.info("Đã xử lý yêu cầu xóa phân loại productId={}, variantId={}", productId, variantsId);
        return ResponseEntity.accepted().build();
    }


}
