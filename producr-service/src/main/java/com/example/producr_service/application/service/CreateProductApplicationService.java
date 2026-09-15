package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.request.VariantImagePosition;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.CreateProductUseCase;
import com.example.producr_service.application.port.out.CurrentUserPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.strategy.UploadPurpose;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Use case tạo product: upload ảnh trước rồi chuyển request đã hoàn chỉnh cho ProductService.
// Chịu trách nhiệm điều phối toàn bộ use case tạo product và upload ảnh liên quan.
@RequiredArgsConstructor
public class CreateProductApplicationService implements CreateProductUseCase {
    private final FileService fileService;
    private final ProductService productService;
    private final CurrentUserPort currentUserPort;

    // Rollback dữ liệu DB và xóa bù trừ file khi use case tạo product thất bại.
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        CreateProductRequest request = command.productRequest();
        List<VariantImageUploadCommand> variantImages = command.variantImages() == null
                ? List.of()
                : command.variantImages();

        // Validate toàn bộ vị trí ảnh trước khi chuẩn bị upload.
        validateVariantImageCommands(request, variantImages);

        List<StoredFile> storedFiles = new java.util.ArrayList<>();

        try {
            // tải ảnh lên store
            uploadProductImage(command.productImage(), storedFiles);
            uploadVariantImages(variantImages, storedFiles);
            // lưu url sau khi tải request
            applyUploadedImageUrls(request, command.productImage(), variantImages, storedFiles);

            Long userId = currentUserPort.getCurrentUserId().userId();
            return ProductResponse.from(productService.createProduct(userId,request));
        } catch (RuntimeException exception) {
            try {
                fileService.deleteStoredFiles(storedFiles);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    // Upload ảnh bìa product theo cấu hình riêng của product image.
    private void uploadProductImage(
            UploadFileCommand productImage,
            List<StoredFile> storedFiles
    ) {
        if (productImage != null) {
            storedFiles.add(fileService.upload(UploadPurpose.PRODUCT_IMAGE, productImage));
        }
    }

    // Upload toàn bộ ảnh variant theo cấu hình riêng của variant image.
    private void uploadVariantImages(
            List<VariantImageUploadCommand> variantImages,
            List<StoredFile> storedFiles
    ) {
        List<UploadFileCommand> variantImageFiles = variantImages.stream()
                .map(VariantImageUploadCommand::file)
                .toList();
        storedFiles.addAll(fileService.upload(UploadPurpose.PRODUCT_VARIANT_IMAGE, variantImageFiles));
    }

    // Kiểm tra toàn bộ vị trí ảnh variant trước khi thực hiện upload.
    private void validateVariantImageCommands(
            CreateProductRequest request,
            List<VariantImageUploadCommand> imageCommands
    ) {
        // tổng số anh upload của các product variant
        int expectedImageCount = countVariantImageSlots(request);
        // validation số lượng ảnh upload đc gửi lên = expectedImageCount
        if (imageCommands.size() != expectedImageCount) {
            throw invalidVariantImageMapping(
                    "So luong variantImages phai khop voi tong so images trong request");
        }

        // list vi trí ảnh nào thuộc variant nào
        Set<VariantImagePosition> usedPositions = new HashSet<>();
        // map list VariantImageUploadCommand sang  Set<VariantImagePosition>
        for (VariantImageUploadCommand imageCommand : imageCommands) {
            int variantIndex = imageCommand.variantIndex();
            int imageIndex = imageCommand.imageIndex();

            if (variantIndex < 0 || variantIndex >= request.getVariants().size()) {
                throw invalidVariantImageMapping("variantIndex khong hop le: " + variantIndex);
            }

            // số ảnh của Variants có index hiện tại đang đc for tới
            int imageCount = request.getVariants().get(variantIndex).getImages().size();

            if (imageIndex < 0 || imageIndex >= imageCount) {
                throw invalidVariantImageMapping(
                        "imageIndex khong hop le: " + imageIndex + " (variant " + variantIndex + ")");
            }

            VariantImagePosition position = new VariantImagePosition(variantIndex, imageIndex);
            if (!usedPositions.add(position)) {
                throw invalidVariantImageMapping(
                        "Khong duoc upload nhieu file cho cung mot vi tri anh: " + position);
            }
        }
    }

    // Đếm tổng số ảnh cần upload của tất cả variant.
    private int countVariantImageSlots(CreateProductRequest request) {
        return request.getVariants().stream()
                .mapToInt(variant -> variant.getImages().size())
                .sum();
    }

    // Gắn các URL upload vào ảnh bìa product và đúng vị trí ảnh variant.  của request
    private void applyUploadedImageUrls(
            CreateProductRequest request,
            UploadFileCommand productImage,
            List<VariantImageUploadCommand> imageCommands,
            List<StoredFile> storedFiles
    ) {
        int urlIndex = 0;
        if (productImage != null) {
            request.setImageUrl(storedFiles.get(urlIndex++).publicUrl());
        }

        for (int i = 0; i < imageCommands.size(); i++) {
            VariantImageUploadCommand imageCommand = imageCommands.get(i);
            request.getVariants()
                    .get(imageCommand.variantIndex())
                    .getImages()
                    .get(imageCommand.imageIndex())
                    .setImageUrl(storedFiles.get(urlIndex + i).publicUrl());
        }
    }

    // Chuẩn hóa lỗi mapping ảnh thành lỗi nghiệp vụ có HTTP 400.
    private BusinessException invalidVariantImageMapping(String detail) {
        return new BusinessException(ProductError.INVALID_VARIANT_IMAGE_MAPPING, detail);
    }
}
