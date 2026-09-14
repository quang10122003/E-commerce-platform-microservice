package com.example.producr_service.adapter.in;

import com.example.producr_service.application.dto.request.CreateProductCommand;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.request.UploadFileCommand;
import com.example.producr_service.application.dto.request.VariantImageMeta;
import com.example.producr_service.application.dto.request.VariantImageUploadCommand;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.port.in.CareteProductUseCase;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CareteProductUseCase careteProductUseCase;

    /**
     * Nhận multipart và chuyển thành application command; không xử lý upload hay nghiệp vụ tại controller.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> create(
            @RequestPart("request") @Valid CreateProductRequest request,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage,
            @RequestPart(value = "variantImages", required = false) List<MultipartFile> variantImages,
            @RequestPart(value = "variantImageMeta", required = false) @Valid List<VariantImageMeta> variantImageMeta
    ) throws IOException {
        // Chuyển dữ liệu HTTP thành command để application service điều phối use case.
        CreateProductCommand command = new CreateProductCommand(
                request,
                toProductImageCommand(productImage),
                toVariantImageCommands(variantImages, variantImageMeta)
        );

        ProductResponse response = careteProductUseCase.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Chuyển ảnh đại diện từ multipart sang command không phụ thuộc Spring Web.
    private UploadFileCommand toProductImageCommand(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return toUploadFileCommand(file, StorageBucket.PRODUCT_IMAGES);
    }

    // Chuyển danh sách ảnh variant và metadata thành các command tương ứng.
    private List<VariantImageUploadCommand> toVariantImageCommands(
            List<MultipartFile> files,
            List<VariantImageMeta> metadata
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        if (metadata == null || metadata.size() != files.size()) {
            throw new IllegalArgumentException(
                    "So luong variantImageMeta phai khop chinh xac voi so luong variantImages");
        }

        List<VariantImageUploadCommand> commands = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            VariantImageMeta meta = metadata.get(i);
            commands.add(new VariantImageUploadCommand(
                    meta.getVariantIndex(),
                    meta.getImageIndex(),
                    toUploadFileCommand(files.get(i), StorageBucket.PRODUCT_VARIANTS)
            ));
        }
        return commands;
    }

    // Đọc multipart file thành command upload dùng chung cho application layer.
    private UploadFileCommand toUploadFileCommand(MultipartFile file, StorageBucket bucket) throws IOException {
        return new UploadFileCommand(
                bucket,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );
    }
}
