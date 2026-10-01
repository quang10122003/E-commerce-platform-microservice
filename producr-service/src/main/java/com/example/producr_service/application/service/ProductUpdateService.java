package com.example.producr_service.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.untill.JsonUtils;
import com.example.common.untill.ValidationUtils;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.command.UpdateProductCommand;
import com.example.producr_service.application.dto.command.VariantImageUploadCommand;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductImagesDeleteOutboxPayload;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.dto.request.UpdateProductData;
import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.ShopProductDetailMapperPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.application.strategy.upload.UploadPurpose;
import com.example.producr_service.domain.model.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.*;

// Đối chiếu dữ liệu chỉnh sửa với sản phẩm hiện có; ProductService giữ transaction.
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductUpdateService {
    ProductRepositoryPort productRepositoryPort;
    ProductVariantRepoPort productVariantRepoPort;
    CategoryRepoPort categoryRepoPort;
    BrandRepositoryPort brandRepositoryPort;
    CurrentUserPort currentUserPort;
    ProductHelper productHelper;
    FileUploader fileUploader;
    ProductImageRollbackPort productImageRollbackPort;
    OutboxPort outboxPort;
    JsonUtils jsonUtils;
    ShopProductDetailMapperPort shopProductDetailMapperPort;

    // Đọc sản phẩm thuộc người dùng hiện tại thành dữ liệu dùng cho form.
    public ShopProductDetailResponse getShopProductDetail(Long productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        if (!product.getUserId().equals(currentUserPort.getUserInfo().userId())) {
            throw new BusinessException(AuthorizationError.ACCESS_DENIED);
        }
        return shopProductDetailMapperPort.toShopProductDetailResponse(product);
    }

    // Khóa sản phẩm và các phân loại trước khi kiểm tra, rồi lưu thay đổi cùng outbox.
    public ShopProductDetailResponse updateProduct(Long productId, UpdateProductCommand command) {
        UserInternaInfoRespone user = currentUserPort.getUserInfo();

        productHelper.validateProductOwnership(productId, user.userId());
        List<ProductVariant> lockedVariants = productVariantRepoPort.findByProductIdForUpdate(productId);

        Product current = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        UpdateProductData request = command.productRequest();
        validateVariantSet(request, lockedVariants);
        validateImageMapping(request, command.variantImages());

        Category category = categoryRepoPort.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ProductError.CATEGORY_NOT_FOUND));
        Brand brand = request.brandId() == null ? null : brandRepositoryPort.findById(request.brandId())
                .orElseThrow(() -> new BusinessException(ProductError.BRAND_NOT_FOUND));

        List<StoredFile> uploaded = new ArrayList<>();
        productImageRollbackPort.deleteImageStore(uploaded);
        StoredFile newCover = command.productImage() == null ? null
                : fileUploader.upload(UploadPurpose.PRODUCT_IMAGE, command.productImage());
        if (newCover != null) uploaded.add(newCover);
        Map<ImagePosition, StoredFile> uploadedImages = uploadVariantImages(command.variantImages(), uploaded);

        // Dựng trạng thái mới nhưng giữ ID, dữ liệu bán hàng và ảnh cũ nếu không thay ảnh bìa.
        Product updated = new Product(current.getId(), current.getUserId(), category.getId(), category.getName(),
                brand == null ? null : brand.getId(), brand == null ? null : brand.getName(),
                request.name(), request.description(),
                newCover == null ? current.getImageUrl() : newCover.publicUrl(),
                current.getCreatedAt(), current.getTotalSold(),
                newCover == null ? current.getObjectPath() : newCover.objectPath());
        // Constructor mặc định ACTIVE nên phải giữ trạng thái tạm ẩn của sản phẩm cũ.
        if (current.getStatus() == ProductStatus.INACTIVE) updated.deactivate();

        // Giữ value theo thứ tự request để mỗi phân loại chọn đúng attributeIndex/valueIndex.
        List<List<AttributeValue>> valuesByIndex = buildAttributes(request, current, updated);
        List<StoredFile> deleteFiles = new ArrayList<>();
        // Chỉ đánh dấu ảnh bìa cũ để dọn sau commit khi có ảnh mới thay thế.
        if (newCover != null) deleteFiles.add(new StoredFile(
                StorageBucket.PRODUCT_IMAGES, current.getObjectPath(), current.getImageUrl()));
        // Dựng tổ hợp cuối cùng để nhóm/value bị bỏ không còn được chọn và các phân loại không trùng.
        buildVariants(request, current, updated, valuesByIndex, uploadedImages, deleteFiles);

        Product saved = productRepositoryPort.update(updated);
        List<StoredFile> filesToDelete = productHelper.prepareFilesForDeletion(productId, deleteFiles);
        if (!filesToDelete.isEmpty()) {
            outboxPort.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT,
                    String.valueOf(productId), ProductImagesDeleteOutboxPayload.EVENT_TYPE,
                    jsonUtils.toJson(new ProductImagesDeleteOutboxPayload(filesToDelete))));
        }
        outboxPort.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT,
                String.valueOf(productId), ProductUpdatedOutboxPayload.EVENT_TYPE,
                jsonUtils.toJson(new ProductUpdatedOutboxPayload(productId,
                        productHelper.extractProvinceNameFromAddress(user.shopAddress())))));
        return shopProductDetailMapperPort.toShopProductDetailResponse(saved);
    }

    // Buộc mọi phân loại cũ có mặt trong request vì API này không xử lý xóa phân loại.
    private void validateVariantSet(UpdateProductData request, List<ProductVariant> currentVariants) {
        if (request.attributes() == null || request.variants() == null || request.variants().isEmpty()) {
            throw invalid("Phai gui attributes va it nhat mot variant");
        }
        Set<Long> currentIds = new HashSet<>();
        for (ProductVariant variant : currentVariants) currentIds.add(variant.getId());
        Set<Long> keptIds = new HashSet<>();
        for (UpdateProductData.Variant variant : request.variants()) {
            if (variant.id() != null && (!currentIds.contains(variant.id()) || !keptIds.add(variant.id()))) {
                throw invalid("Variant ID khong thuoc san pham hoac bi trung");
            }
        }
        // So sánh toàn bộ ID cũ để PUT không vô tình xóa phân loại bị thiếu trong form.
        if (!keptIds.equals(currentIds)) throw invalid("Phai giu tat ca variant cu trong request");
    }

    // Đảm bảo mỗi ảnh mới có đúng một file theo vị trí, không gắn file cho ảnh cũ.
    private void validateImageMapping(UpdateProductData request, List<VariantImageUploadCommand> commands) {
        List<VariantImageUploadCommand> files = commands == null ? List.of() : commands;
        Set<ImagePosition> slots = new HashSet<>();
        for (int variantIndex = 0; variantIndex < request.variants().size(); variantIndex++) {
            List<UpdateProductData.Image> images = request.variants().get(variantIndex).images();
            if (images == null) throw invalid("Phai gui images cho moi variant");
            for (int imageIndex = 0; imageIndex < images.size(); imageIndex++) {
                if (images.get(imageIndex).id() == null) slots.add(new ImagePosition(variantIndex, imageIndex));
            }
        }
        Set<ImagePosition> mapped = new HashSet<>();
        for (VariantImageUploadCommand file : files) {
            ImagePosition position = new ImagePosition(file.variantIndex(), file.imageIndex());
            if (!slots.contains(position) || !mapped.add(position)) {
                throw new BusinessException(ProductError.INVALID_VARIANT_IMAGE_MAPPING);
            }
        }
        if (!mapped.equals(slots)) throw new BusinessException(ProductError.INVALID_VARIANT_IMAGE_MAPPING);
    }

    // Lưu ảnh phân loại mới và giữ vị trí để gắn đúng ảnh sau khi dựng sản phẩm.
    private Map<ImagePosition, StoredFile> uploadVariantImages(
            List<VariantImageUploadCommand> commands, List<StoredFile> uploaded) {
        Map<ImagePosition, StoredFile> result = new HashMap<>();
        if (commands == null) return result;
        for (VariantImageUploadCommand command : commands) {
            StoredFile file = fileUploader.upload(UploadPurpose.PRODUCT_VARIANT_IMAGE, command.file());
            uploaded.add(file);
            result.put(new ImagePosition(command.variantIndex(), command.imageIndex()), file);
        }
        return result;
    }

    // Giữ ID thuộc tính cũ, thêm giá trị mới và đối chiếu ID với sản phẩm hiện tại.
    private List<List<AttributeValue>> buildAttributes(
            UpdateProductData request, Product current, Product updated) {
        Map<Long, ProductAttribute> oldAttributes = new HashMap<>();
        Map<Long, AttributeValue> oldValues = new HashMap<>();
        for (ProductAttribute attribute : current.getAttributes()) {
            oldAttributes.put(attribute.getId(), attribute);
            for (AttributeValue value : attribute.getValues()) oldValues.put(value.getId(), value);
        }
        Set<Long> usedAttributes = new HashSet<>();
        Set<Long> usedValues = new HashSet<>();
        Set<String> attributeNames = new HashSet<>();
        List<List<AttributeValue>> valuesByIndex = new ArrayList<>();
        for (UpdateProductData.Attribute data : request.attributes()) {
            if (!ValidationUtils.hasText(data.name()) || data.values() == null
                    || data.values().isEmpty()
                    || !attributeNames.add(ValidationUtils.normalize(data.name()).toLowerCase(Locale.ROOT))) {
                throw invalid("Nhom thuoc tinh trong hoac bi trung");
            }
            if (data.id() != null && (!oldAttributes.containsKey(data.id()) || !usedAttributes.add(data.id()))) {
                throw invalid("Attribute ID khong hop le");
            }
            ProductAttribute attribute = new ProductAttribute(data.id(), data.name());
            Set<String> valueNames = new HashSet<>();
            List<AttributeValue> values = new ArrayList<>();
            for (UpdateProductData.Value valueData : data.values()) {
                if (!ValidationUtils.hasText(valueData.value())
                        || !valueNames.add(ValidationUtils.normalize(valueData.value())
                        .toLowerCase(Locale.ROOT))) {
                    throw invalid("Gia tri thuoc tinh bi trung");
                }
                if (valueData.id() != null && (data.id() == null
                        || !oldAttributes.get(data.id()).getValues().contains(oldValues.get(valueData.id()))
                        || !usedValues.add(valueData.id()))) {
                    throw invalid("Attribute value ID khong hop le");
                }
                AttributeValue value = new AttributeValue(valueData.id(), valueData.value());
                attribute.addValue(value);
                values.add(value);
            }
            updated.addAttribute(attribute);
            valuesByIndex.add(values);
        }
        return valuesByIndex;
    }

    // Giữ SKU của phân loại cũ, sinh SKU một lần cho phân loại mới và gom ảnh cần dọn.
    private void buildVariants(UpdateProductData request, Product current, Product updated,
                               List<List<AttributeValue>> valuesByIndex,
                               Map<ImagePosition, StoredFile> uploadedImages,
                               List<StoredFile> obsoleteFiles) {
        Map<Long, ProductVariant> oldVariants = new HashMap<>();
        for (ProductVariant variant : current.getVariants()) oldVariants.put(variant.getId(), variant);
        Set<Set<AttributeValue>> combinations = new HashSet<>();
        Set<String> skus = new HashSet<>();
        current.getVariants().forEach(variant -> skus.add(variant.getSku()));
        for (int index = 0; index < request.variants().size(); index++) {
            UpdateProductData.Variant data = request.variants().get(index);
            List<AttributeValue> selections = resolveSelections(data, valuesByIndex);
            if (!combinations.add(Set.copyOf(selections))) {
                throw new BusinessException(ProductError.DUPLICATE_VARIANT_ATTRIBUTE_COMBINATION);
            }
            ProductVariant old = data.id() == null ? null : oldVariants.get(data.id());
            String sku = old == null ? productHelper.generateUniqueSku(request.name(),
                    selections.stream().map(AttributeValue::getValue).toList(), null, skus) : old.getSku();
            ProductVariant variant = new ProductVariant(data.id(), sku,
                    Money.of(data.price()), data.stockQuantity());
            selections.forEach(variant::linkAttributeValue);
            addImages(index, data.images(), old, variant, uploadedImages, obsoleteFiles);
            updated.addVariant(variant);
        }
    }

    // Yêu cầu mỗi phân loại chọn đúng một giá trị của từng nhóm thuộc tính.
    private List<AttributeValue> resolveSelections(UpdateProductData.Variant variant,
                                                   List<List<AttributeValue>> valuesByIndex) {
        if (variant.attributeSelections() == null
                || variant.attributeSelections().size() != valuesByIndex.size()) {
            throw new BusinessException(ProductError.INVALID_VARIANT_ATTRIBUTES);
        }
        Map<Integer, AttributeValue> selected = new HashMap<>();
        for (UpdateProductData.Selection selection : variant.attributeSelections()) {
            int group = selection.attributeIndex();
            int value = selection.valueIndex();
            if (group < 0 || group >= valuesByIndex.size() || value < 0
                    || value >= valuesByIndex.get(group).size()
                    || selected.putIfAbsent(group, valuesByIndex.get(group).get(value)) != null) {
                throw new BusinessException(ProductError.INVALID_ATTRIBUTE_SELECTION);
            }
        }
        List<AttributeValue> result = new ArrayList<>();
        for (int index = 0; index < valuesByIndex.size(); index++) result.add(selected.get(index));
        return result;
    }

    // Giữ ảnh theo ID, gắn ảnh mới theo vị trí và đánh dấu ảnh bị bỏ khỏi phân loại.
    private void addImages(int variantIndex, List<UpdateProductData.Image> images,
                           ProductVariant old, ProductVariant updated,
                           Map<ImagePosition, StoredFile> uploads, List<StoredFile> obsoleteFiles) {
        Map<Long, VariantImage> oldImages = new HashMap<>();
        if (old != null) for (VariantImage image : old.getImages()) oldImages.put(image.getId(), image);
        Set<Long> retained = new HashSet<>();
        for (int imageIndex = 0; imageIndex < images.size(); imageIndex++) {
            UpdateProductData.Image data = images.get(imageIndex);
            if (data.id() != null) {
                VariantImage existing = oldImages.get(data.id());
                if (existing == null || !retained.add(data.id())) throw invalid("Variant image ID khong hop le");
                updated.addImage(new VariantImage(existing.getId(), existing.getImageUrl(),
                        existing.getObjectPath(), data.primary()));
            } else {
                StoredFile file = uploads.get(new ImagePosition(variantIndex, imageIndex));
                updated.addImage(new VariantImage(null, file.publicUrl(), file.objectPath(), data.primary()));
            }
        }
        for (VariantImage image : oldImages.values()) {
            if (!retained.contains(image.getId())) obsoleteFiles.add(storedVariantImage(image));
        }
    }

    // Giữ bucket, đường dẫn và URL của ảnh cũ để outbox dọn đúng file.
    private StoredFile storedVariantImage(VariantImage image) {
        return new StoredFile(StorageBucket.PRODUCT_VARIANTS, image.getObjectPath(), image.getImageUrl());
    }

    // Trả lỗi 400 thống nhất cho dữ liệu chỉnh sửa không hợp lệ.
    private BusinessException invalid(String detail) {
        return new BusinessException(ProductError.INVALID_PRODUCT_UPDATE, detail);
    }

    private record ImagePosition(int variantIndex, int imageIndex) {
    }
}
