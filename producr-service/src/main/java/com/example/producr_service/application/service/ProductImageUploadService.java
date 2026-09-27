package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.request.CreateProductData;
import com.example.producr_service.application.dto.request.VariantImagePosition;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.strategy.upload.UploadPurpose;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Xử lý validate, upload và gắn thông tin lưu trữ cho ảnh product.
@RequiredArgsConstructor
public class ProductImageUploadService {

    private final FileService fileService;

    // Chuẩn bị toàn bộ ảnh product và giữ file đã upload để caller có thể rollback.
    public void uploadAndApplyImageUrls(
            CreateProductData request,
            UploadFileCommand productImage,
            List<VariantImageUploadCommand> variantImages,
            List<StoredFile> storedFiles
    ) {
        validateVariantImageCommands(request, variantImages);
        uploadProductImage(productImage, storedFiles);
        uploadVariantImages(variantImages, storedFiles);
        applyUploadedImages(request, productImage, variantImages, storedFiles);
    }

    // Upload ảnh bìa product theo cấu hình riêng của product image.
    private void uploadProductImage(
            UploadFileCommand productImage,
            List<StoredFile> storedFiles
    ) {
        // Ảnh đại diện bắt buộc phải được upload trước khi lưu product.
        if (productImage == null) {
            throw new BusinessException(ProductError.INVALID_IMAGE_FILE,
                    "File anh dai dien khong duoc de trong");
        }

        storedFiles.add(fileService.upload(UploadPurpose.PRODUCT_IMAGE, productImage));
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
            CreateProductData request,
            List<VariantImageUploadCommand> imageCommands
    ) {
        // Đảm bảo số file gửi lên khớp với toàn bộ vị trí ảnh variant đã khai báo.
        int expectedImageCount = countVariantImageSlots(request);
        if (imageCommands.size() != expectedImageCount) {
            throw invalidVariantImageMapping(
                    "So luong variantImages phai khop voi tong so images trong request");
        }

        // Ngăn một vị trí ảnh bị gắn nhiều file upload.
        Set<VariantImagePosition> usedPositions = new HashSet<>();
        for (VariantImageUploadCommand imageCommand : imageCommands) {
            int variantIndex = imageCommand.variantIndex();
            int imageIndex = imageCommand.imageIndex();

            if (variantIndex < 0 || variantIndex >= request.getVariants().size()) {
                throw invalidVariantImageMapping("variantIndex khong hop le: " + variantIndex);
            }

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
    private int countVariantImageSlots(CreateProductData request) {
        return request.getVariants().stream()
                .mapToInt(variant -> variant.getImages().size())
                .sum();
    }

    // Gắn URL và object path cho ảnh bìa cùng từng vị trí ảnh variant.
    private void applyUploadedImages(
            CreateProductData request,
            UploadFileCommand productImage,
            List<VariantImageUploadCommand> imageCommands,
            List<StoredFile> storedFiles
    ) {
        int urlIndex = 0;
        if (productImage != null) {
            StoredFile storedFile = storedFiles.get(urlIndex++);
            request.setImageUrl(storedFile.publicUrl());
            request.setObjectPath(storedFile.objectPath());
        }

        for (int i = 0; i < imageCommands.size(); i++) {
            VariantImageUploadCommand imageCommand = imageCommands.get(i);
            CreateProductData.VariantImageRequest imageRequest = request.getVariants()
                    .get(imageCommand.variantIndex())
                    .getImages()
                    .get(imageCommand.imageIndex());
            StoredFile storedFile = storedFiles.get(urlIndex + i);
            imageRequest.setImageUrl(storedFile.publicUrl());
            imageRequest.setObjectPath(storedFile.objectPath());
        }
    }

    // Chuẩn hóa lỗi mapping ảnh thành lỗi nghiệp vụ có HTTP 400.
    private BusinessException invalidVariantImageMapping(String detail) {
        return new BusinessException(ProductError.INVALID_VARIANT_IMAGE_MAPPING, detail);
    }
}
