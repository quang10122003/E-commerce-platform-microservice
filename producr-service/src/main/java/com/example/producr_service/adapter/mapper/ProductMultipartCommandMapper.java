package com.example.producr_service.adapter.mapper;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.command.CreateProductCommand;
import com.example.producr_service.application.dto.command.UpdateProductCommand;
import com.example.producr_service.adapter.DTO.request.CreateProductRequest;
import com.example.producr_service.adapter.DTO.request.UpdateProductRequest;
import com.example.producr_service.application.dto.request.CreateProductData;
import com.example.producr_service.application.dto.request.UpdateProductData;
import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.adapter.DTO.request.VariantImageMeta;
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
                toProductData(request),
                toProductImageCommand(productImage),
                toVariantImageCommands(variantImages, variantImageMeta)
        );
    }

    // Giữ vị trí từng ảnh mới khi chuyển form multipart sang dữ liệu xử lý nội bộ.
    public UpdateProductCommand toUpdateCommand(
            UpdateProductRequest request,
            MultipartFile productImage,
            List<MultipartFile> variantImages,
            List<VariantImageMeta> variantImageMeta
    ) throws IOException {
        for (UpdateProductRequest.Variant variant : request.getVariants()) {
            if (variant.getSku() != null) {
                throw new BusinessException(ProductError.INVALID_VARIANT_ATTRIBUTES,
                        "Khong duoc gui sku khi cap nhat san pham");
            }
        }
        UpdateProductData data = new UpdateProductData(
                request.getCategoryId(), request.getBrandId(), request.getName(), request.getDescription(),
                request.getAttributes().stream()
                        .map(attribute -> new UpdateProductData.Attribute(attribute.getId(), attribute.getName(),
                                attribute.getValues().stream()
                                        .map(value -> new UpdateProductData.Value(value.getId(), value.getValue()))
                                        .toList()))
                        .toList(),
                request.getVariants().stream()
                        .map(variant -> new UpdateProductData.Variant(variant.getId(), variant.getPrice(),
                                variant.getStockQuantity(),
                                variant.getAttributeSelections().stream()
                                        .map(selection -> new UpdateProductData.Selection(
                                                selection.getAttributeIndex(), selection.getValueIndex()))
                                        .toList(),
                                variant.getImages().stream()
                                        .map(image -> new UpdateProductData.Image(image.getId(), image.isPrimary()))
                                        .toList()))
                        .toList()
        );
        return new UpdateProductCommand(data,
                productImage == null || productImage.isEmpty() ? null : toUploadFileCommand(productImage),
                toVariantImageCommands(variantImages, variantImageMeta));
    }

    // Chuyển dữ liệu HTTP sang DTO nội bộ, không đưa validation vào application.
    private CreateProductData toProductData(CreateProductRequest request) {
        CreateProductData data = new CreateProductData();
        data.setCategoryId(request.getCategoryId());
        data.setBrandId(request.getBrandId());
        data.setName(request.getName());
        data.setDescription(request.getDescription());
        if (request.getAttributes() != null) {
            data.setAttributes(request.getAttributes().stream().map(attribute -> {
                CreateProductData.AttributeRequest mapped = new CreateProductData.AttributeRequest();
                mapped.setName(attribute.getName());
                mapped.setValues(attribute.getValues());
                return mapped;
            }).toList());
        }
        if (request.getVariants() != null) {
            data.setVariants(request.getVariants().stream().map(variant -> {
                CreateProductData.VariantRequest mapped = new CreateProductData.VariantRequest();
                mapped.setPrice(variant.getPrice());
                mapped.setStockQuantity(variant.getStockQuantity());
                if (variant.getAttributeSelections() != null) {
                    mapped.setAttributeSelections(variant.getAttributeSelections().stream().map(selection -> {
                        CreateProductData.AttributeSelection item = new CreateProductData.AttributeSelection();
                        item.setAttributeIndex(selection.getAttributeIndex());
                        item.setValueIndex(selection.getValueIndex());
                        return item;
                    }).toList());
                }
                if (variant.getImages() != null) {
                    mapped.setImages(variant.getImages().stream().map(image -> {
                        CreateProductData.VariantImageRequest item = new CreateProductData.VariantImageRequest();
                        item.setPrimary(image.isPrimary());
                        return item;
                    }).toList());
                }
                return mapped;
            }).toList());
        }
        return data;
    }

    // Chuyển ảnh đại diện từ multipart sang command
    private UploadFileCommand toProductImageCommand(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ProductError.INVALID_IMAGE_FILE,
                    "File anh dai dien khong duoc de trong");
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
