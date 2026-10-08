package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.FlashSaleCampaignEntity;
import com.example.producr_service.application.dto.response.FlashSaleCampaignView;
import com.example.producr_service.domain.model.FlashSaleCampaign;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.example.producr_service.adapter.entity.FlashSaleItemEntity;
import com.example.producr_service.adapter.entity.ProductVariantEntity;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FlashSaleCampaignMapper {
    private final FlashSaleItemMapper itemMapper;

    // Tạo entity cho aggregate mới; aggregate đã lưu được cập nhật tại chỗ.
    public FlashSaleCampaignEntity toEntity(FlashSaleCampaign campaign,
            Function<Long, ProductVariantEntity> variantReference) {
        FlashSaleCampaignEntity entity = new FlashSaleCampaignEntity();
        entity.setId(campaign.getId());
        updateEntity(campaign, entity, variantReference);
        return entity;
    }

    // Map các trường của chương trình; collection được đồng bộ riêng bên dưới.
    private void updateHeader(FlashSaleCampaign campaign, FlashSaleCampaignEntity entity) {
        entity.setName(campaign.getName());
        entity.setDescription(campaign.getDescription());
        entity.setRegistrationDeadline(campaign.getRegistrationDeadline());
        entity.setStartsAt(campaign.getStartsAt());
        entity.setEndsAt(campaign.getEndsAt());
        entity.setStatus(campaign.getStatus());
        entity.setCreatedBy(campaign.getCreatedBy());
        entity.setCreatedAt(campaign.getCreatedAt());
        entity.setUpdatedAt(campaign.getUpdatedAt());
    }

    // Dựng aggregate đầy đủ để các thao tác cập nhật luôn giữ cả đăng ký hiện có.
    public FlashSaleCampaign toDomain(FlashSaleCampaignEntity entity) {
        return FlashSaleCampaign.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getRegistrationDeadline(),
                entity.getStartsAt(),
                entity.getEndsAt(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getItems().stream().map(itemMapper::toDomain).toList()
        );
    }

    // Đọc phần chương trình mà không truy cập collection LAZY khi phân trang.
    public FlashSaleCampaignView toView(FlashSaleCampaignEntity entity) {
        return new FlashSaleCampaignView(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getRegistrationDeadline(), entity.getStartsAt(), entity.getEndsAt(), entity.getStatus(),
                entity.getCreatedBy(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    // Đồng bộ tại chỗ để orphanRemoval chỉ xóa item thực sự bị domain loại khỏi collection.
    public void updateEntity(FlashSaleCampaign campaign, FlashSaleCampaignEntity entity,
            Function<Long, ProductVariantEntity> variantReference) {
        updateHeader(campaign, entity);
        var existing = entity.getItems().stream()
                .collect(Collectors.toMap(FlashSaleItemEntity::getId, Function.identity()));
        var retainedIds = campaign.getItems().stream().map(item -> item.id())
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        if (!existing.keySet().containsAll(retainedIds)) {
            throw new IllegalStateException("Item cần cập nhật không thuộc collection hiện tại");
        }
        entity.getItems().removeIf(item -> !retainedIds.contains(item.getId()));
        for (var item : campaign.getItems()) {
            ProductVariantEntity variant = item.variantId() == null ? null : variantReference.apply(item.variantId());
            if (item.id() == null) {
                entity.getItems().add(itemMapper.toEntity(item, entity, variant));
            } else {
                itemMapper.updateEntity(item, existing.get(item.id()), entity, variant);
            }
        }
    }

}
