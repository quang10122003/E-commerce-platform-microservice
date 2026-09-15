package com.example.producr_service.adapter.mapper;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.dto.request.VariantImageMeta;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.error.ProductError;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Chuyển multipart của Spring Web thành command độc lập cho application layer.
@Component
public class ProductMultipartCommandMapper {

    // Gom request JSON và các file multipart thành command tạo Product.
    public CreateProductCommand toCommand(
            CreateProductRequest request,
            MultipartFile productImage,
            List<MultipartFile> variantImages,
            List<VariantImageMeta> variantImageMeta
    ) throws IOException {
        return new CreateProductCommand(
                request,
                toProductImageCommand(productImage),
                toVariantImageCommands(variantImages, variantImageMeta)
        );
    }

    // Chuyển ảnh đại diện từ multipart sang command
    private UploadFileCommand toProductImageCommand(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be null");
        }
        return toUploadFileCommand(file);
    }

    // Chuyển danh sách ảnh variant và metadata thành các command tương ứng.
    private List<VariantImageUploadCommand> toVariantImageCommands(
            List<MultipartFile> files,
            List<VariantImageMeta> metadata
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            // Không có slot ảnh variant thì không cần command upload tương ứng.
            if (metadata == null || metadata.isEmpty()) {
                return List.of();
            }
            throw new BusinessException(
                    ProductError.INVALID_VARIANT_IMAGE_MAPPING,
                    "Khong duoc gui variantImageMeta khi khong co variantImages"
            );
        }
        if (metadata == null || metadata.size() != files.size()) {
            throw new BusinessException(
                    ProductError.INVALID_VARIANT_IMAGE_MAPPING,
                    "So luong variantImageMeta phai khop chinh xac voi so luong variantImages");
        }

        List<VariantImageUploadCommand> commands = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            VariantImageMeta meta = metadata.get(index);
            commands.add(new VariantImageUploadCommand(
                    meta.getVariantIndex(),
                    meta.getImageIndex(),
                    toUploadFileCommand(files.get(index))
            ));
        }
        return commands;
    }

    // Đọc multipart file thành command upload dùng chung cho application layer.
    private UploadFileCommand toUploadFileCommand(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ProductError.INVALID_IMAGE_FILE,
                    "File anh khong duoc de trong");
        }
        return new UploadFileCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );
    }
}
