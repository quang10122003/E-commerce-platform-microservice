package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.FlashSaleCampaignEntity;
import com.example.producr_service.domain.model.FlashSaleCampaign;
import org.springframework.stereotype.Component;

@Component
public class FlashSaleCampaignMapper {

    // Chuyển chương trình sale domain thành entity để lưu xuống database.
    public FlashSaleCampaignEntity toEntity(FlashSaleCampaign campaign) {
        FlashSaleCampaignEntity entity = new FlashSaleCampaignEntity();
        entity.setId(campaign.getId());
        entity.setName(campaign.getName());
        entity.setDescription(campaign.getDescription());
        entity.setRegistrationDeadline(campaign.getRegistrationDeadline());
        entity.setStartsAt(campaign.getStartsAt());
        entity.setEndsAt(campaign.getEndsAt());
        entity.setStatus(campaign.getStatus());
        entity.setCreatedBy(campaign.getCreatedBy());
        entity.setCreatedAt(campaign.getCreatedAt());
        entity.setUpdatedAt(campaign.getUpdatedAt());
        return entity;
    }

    // Dựng lại domain từ entity đã lưu, giữ nguyên trạng thái và thời gian.
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
                entity.getUpdatedAt()
        );
    }
}
