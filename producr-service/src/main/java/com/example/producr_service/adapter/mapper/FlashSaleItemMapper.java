package com.example.producr_service.adapter.mapper;

import com.example.common.untill.JsonUtils;
import com.example.producr_service.adapter.entity.*;
import com.example.producr_service.application.dto.response.FlashSaleVariantSnapshot;
import com.example.producr_service.application.dto.response.UserFlashSaleItemView;
import com.example.producr_service.domain.model.FlashSaleItem;
import com.example.producr_service.domain.model.ProductStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class FlashSaleItemMapper {
     JsonUtils jsonUtils;
     ProductVariantMapper productVariantMapper;

    // Chuyển dữ liệu JPA thành snapshot phục vụ kiểm tra và lưu đăng ký.
    public FlashSaleVariantSnapshot toSnapshot(ProductVariantEntity variant) {
        var product = variant.getProduct();
        var attributes = variant.getAttributeValues().stream()
                .sorted(Comparator.comparing((AttributeValueEntity value) -> value.getAttribute().getId())
                        .thenComparing(AttributeValueEntity::getId))
                .map(value -> new FlashSaleVariantSnapshot.Attribute(
                        value.getAttribute().getName(), value.getValue())).toList();
        return new FlashSaleVariantSnapshot(variant.getId(), product.getUserId(), product.getName(),
                product.getStatus() == ProductStatus.ACTIVE, variant.getPrice(),
                variant.getStockQuantity(), attributes);
    }

    // Chuyển đăng ký domain thành entity; JSON chỉ được xử lý ở adapter.
    public FlashSaleItemEntity toEntity(FlashSaleItem item,
            FlashSaleCampaignEntity campaign, ProductVariantEntity variantEntity) {
        var entity = new FlashSaleItemEntity();
        updateEntity(item, entity, campaign, variantEntity);
        return entity;
    }

    // Cập nhật entity đang được quản lý, giữ danh tính để không tạo lại item cũ.
    public void updateEntity(FlashSaleItem item, FlashSaleItemEntity entity,
            FlashSaleCampaignEntity campaign, ProductVariantEntity variantEntity) {
        entity.setCampaign(campaign);
        entity.setVariant(variantEntity);
        entity.setShopId(item.shopId());
        entity.setProductName(item.productName());
        entity.setVariantAttributes(jsonUtils.toJson(item.attributes()));
        entity.setDiscountPercent(item.discountPercent());
        entity.setRegisteredQuantity(item.registeredQuantity());
        entity.setSoldQuantity(item.soldQuantity());
        entity.setStatus(item.status());
        entity.setCreatedAt(item.createdAt());
        entity.setUpdatedAt(item.updatedAt());
    }

    // Đọc snapshot TEXT qua JsonUtils và dựng lại domain, kể cả variant đã bị xóa.
    public FlashSaleItem toDomain(FlashSaleItemEntity entity) {
        Object decoded = jsonUtils.fromJson(entity.getVariantAttributes());
        if (!(decoded instanceof java.util.List<?> values)) {
            throw new IllegalStateException("Snapshot thuộc tính không phải danh sách");
        }
        var attributes = values.stream().map(value -> {
            if (!(value instanceof java.util.Map<?, ?> attribute)
                    || !(attribute.get("name") instanceof String name)
                    || !(attribute.get("value") instanceof String text)) {
                throw new IllegalStateException("Snapshot thuộc tính không hợp lệ");
            }
            return new FlashSaleItem.Attribute(name, text);
        }).toList();
        return new FlashSaleItem(entity.getId(), entity.getCampaign().getId(),
                entity.getVariant() == null ? null : entity.getVariant().getId(), entity.getShopId(),
                entity.getProductName(), attributes, entity.getDiscountPercent(), entity.getRegisteredQuantity(),
                entity.getSoldQuantity(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }


    public UserFlashSaleItemView toUserFlashSaleItemView(FlashSaleItemEntity  flashSaleItemEntity){
        return new UserFlashSaleItemView(productVariantMapper.toCartView(flashSaleItemEntity.getVariant()),flashSaleItemEntity.getDiscountPercent(),flashSaleItemEntity.getRegisteredQuantity(),flashSaleItemEntity.getSoldQuantity(),flashSaleItemEntity.getCampaign().getEndsAt());
    }
}
