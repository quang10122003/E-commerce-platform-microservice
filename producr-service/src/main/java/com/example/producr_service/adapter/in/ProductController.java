package com.example.producr_service.adapter.in;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.mapper.ProductMultipartCommandMapper;
import com.example.producr_service.application.dto.request.*;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import com.example.producr_service.application.port.in.GetSellerProductsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.query.Page;
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
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {

    CreateProductUseCase createProductUseCase;
    GetProductsCatalogUseCase getProductsCatalogUseCase;
    ProductMultipartCommandMapper productMultipartCommandMapper;
    GetSellerProductsUseCase getSellerProductsUseCase;


    //  api tạo sản phẩm của shop
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("request") @Valid CreateProductRequest request,
            @RequestPart("productImage") MultipartFile productImage,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages,
            @RequestPart(value = "variantImageMeta", required = false) @Valid List<VariantImageMeta> variantImageMeta
    ) throws IOException {
        ProductResponse response = createProductUseCase.createProduct(
                productMultipartCommandMapper.toCommand(
                        request,
                        productImage,
                        variantImages,
                        variantImageMeta
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // API lấy catalog product đang hoạt động bằng cơ chế infinity scroll.
    @GetMapping()
    public ResponseEntity<ProductCatalogSearchResponse> getProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
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

}
