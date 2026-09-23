package com.example.producr_service.adapter.in;

import com.example.producr_service.adapter.mapper.ProductMultipartCommandMapper;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.ProductSortOption;
import com.example.producr_service.application.dto.request.VariantImageMeta;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.in.GetProductsCatalogUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {

    CreateProductUseCase createProductUseCase;
    GetProductsCatalogUseCase getProductsCatalogUseCase;
    ProductMultipartCommandMapper productMultipartCommandMapper;


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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(required = false) @DecimalMin("0") BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin("0") BigDecimal maxPrice,
            @RequestParam(required = false) String cursor,
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
                size,
                sortOption
        );

        return ResponseEntity.ok(getProductsCatalogUseCase.getProducts(filter));
    }

}
