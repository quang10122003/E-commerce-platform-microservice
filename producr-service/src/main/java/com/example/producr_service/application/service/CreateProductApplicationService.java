package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.request.CreateProductCommand;
import com.example.producr_service.application.dto.request.CreateProductRequest;
import com.example.producr_service.application.dto.request.UploadFileCommand;
import com.example.producr_service.application.dto.request.VariantImageUploadCommand;
import com.example.producr_service.application.dto.response.ProductResponse;
import com.example.producr_service.application.port.in.CareteProductUseCase;

import java.util.List;

// Use case tạo product: upload ảnh trước rồi chuyển request đã hoàn chỉnh cho ProductService.
// Chịu trách nhiệm điều phối toàn bộ use case tạo product và upload ảnh liên quan.
public class CreateProductApplicationService implements CareteProductUseCase {
    private final FileService fileService;
    private final ProductService productService;

    public CreateProductApplicationService(FileService fileService, ProductService productService) {
        this.fileService = fileService;
        this.productService = productService;
    }

    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        CreateProductRequest request = command.productRequest();
        List<VariantImageUploadCommand> variantImages = command.variantImages() == null
                ? List.of()
                : command.variantImages();

        // Validate toàn bộ vị trí ảnh trước khi chuẩn bị upload.
        validateVariantImageCommands(request, variantImages);

        // Gom tất cả file để FileService validate toàn bộ trước khi upload bất kỳ file nào.
        List<UploadFileCommand> uploadCommands = new java.util.ArrayList<>();
        if (command.productImage() != null) {
            uploadCommands.add(command.productImage());
        }
        variantImages.forEach(image -> uploadCommands.add(image.file()));

        List<String> uploadedUrls = fileService.upload(uploadCommands);
        int urlIndex = 0;
        if (command.productImage() != null) {
            request.setImageUrl(uploadedUrls.get(urlIndex++));
        }
        applyVariantImageUrls(request, variantImages, uploadedUrls, urlIndex);

        return productService.createProduct(request);
    }

    // Kiểm tra toàn bộ vị trí ảnh variant trước khi thực hiện upload.
    private void validateVariantImageCommands(
            CreateProductRequest request,
            List<VariantImageUploadCommand> imageCommands
    ) {
        if (imageCommands == null || imageCommands.isEmpty()) {
            return;
        }

        for (VariantImageUploadCommand imageCommand : imageCommands) {
            int variantIndex = imageCommand.variantIndex();
            int imageIndex = imageCommand.imageIndex();

            if (variantIndex < 0 || variantIndex >= request.getVariants().size()) {
                throw new IllegalArgumentException("variantIndex khong hop le: " + variantIndex);
            }

            int imageCount = request.getVariants().get(variantIndex).getImages().size();
            if (imageIndex < 0 || imageIndex >= imageCount) {
                throw new IllegalArgumentException(
                        "imageIndex khong hop le: " + imageIndex + " (variant " + variantIndex + ")");
            }

        }
    }

    // Gắn URL đã upload vào đúng ảnh variant theo thứ tự command ban đầu.
    private void applyVariantImageUrls(
            CreateProductRequest request,
            List<VariantImageUploadCommand> imageCommands,
            List<String> uploadedUrls,
            int startIndex
    ) {
        for (int i = 0; i < imageCommands.size(); i++) {
            VariantImageUploadCommand imageCommand = imageCommands.get(i);
            request.getVariants()
                    .get(imageCommand.variantIndex())
                    .getImages()
                    .get(imageCommand.imageIndex())
                    .setImageUrl(uploadedUrls.get(startIndex + i));
        }
    }
}
