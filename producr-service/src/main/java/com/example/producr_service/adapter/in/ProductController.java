package com.example.producr_service.adapter.in;

import com.example.producr_service.adapter.client.AuthUserFeignClient;
import com.example.producr_service.adapter.mapper.ProductMultipartCommandMapper;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.request.VariantImageMeta;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {

    CreateProductUseCase createProductUseCase;
    ProductMultipartCommandMapper productMultipartCommandMapper;
    AuthUserFeignClient authUserFeignClient;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("request") @Valid CreateProductRequest request,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage,
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

}
