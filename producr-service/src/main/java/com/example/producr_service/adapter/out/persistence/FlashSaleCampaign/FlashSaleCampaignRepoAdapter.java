package com.example.producr_service.adapter.out.persistence.FlashSaleCampaign;

import com.example.producr_service.adapter.mapper.FlashSaleCampaignMapper;
import com.example.producr_service.adapter.out.persistence.ProductVariant.ProductVariantJpa;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.dto.response.FlashSaleCampaignView;
import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleCampaignRepoAdapter implements FlashSaleCampaignRepositoryPort {
    FlashSaleCampaignJpa flashSaleCampaignJpa;
    FlashSaleCampaignMapper flashSaleCampaignMapper;
    ProductVariantJpa productVariantJpa;

    // Dùng findById có sẵn của JPA, chỉ map thông tin chương trình.
    @Override
    public Optional<FlashSaleCampaignView> findViewById(Long campaignId) {
        return flashSaleCampaignJpa.findById(campaignId).map(flashSaleCampaignMapper::toView);
    }

    // Kiểm tra lịch trùng, bỏ qua chương trình đã hủy hoặc hoàn thành.
    @Override
    public boolean existsOverlapping(LocalDateTime startsAt, LocalDateTime endsAt) {
        return flashSaleCampaignJpa.existsOverlapping(
                startsAt, endsAt, List.of(FlashSaleCampaignStatus.CANCELLED, FlashSaleCampaignStatus.DONE));
    }

    // Tái sử dụng index trang của PageQuery; thứ tự và bộ lọc được áp dụng ngay trong DB.
    @Override
    public PageResponse<FlashSaleCampaignView> findAll(PageQuery pageQuery, LocalDateTime from,
                                                 LocalDateTime to, FlashSaleCampaignStatus status,
                                                 LocalDateTime now, Boolean registrationOpen) {
        var entityPage = flashSaleCampaignJpa.findCampaigns(
                from, to, status == null ? null : status.name(), now, registrationOpen,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size())
        );
        return new PageResponse<>(
                entityPage.getContent().stream().map(flashSaleCampaignMapper::toView).toList(),
                entityPage.getNumber() + 1,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages()
        );
    }

    // Khóa parent trước khi tải items để các request cập nhật không ghi đè nhau.
    @Override
    public Optional<FlashSaleCampaign> findByIdForUpdate(Long campaignId) {
        return flashSaleCampaignJpa.findByIdForUpdate(campaignId).map(flashSaleCampaignMapper::toDomain);
    }

    // Khóa chia sẻ trong transaction rồi chuyển dữ liệu thành domain.
    @Override
    public Optional<FlashSaleCampaign> findByIdForShare(Long campaignId) {
        return flashSaleCampaignJpa.findByIdForShare(campaignId).map(flashSaleCampaignMapper::toDomain);
    }

    // Đồng bộ aggregate đầy đủ trên entity đang quản lý, giữ ID và lịch sử của item.
    @Override
    public void save(FlashSaleCampaign campaign) {
        if (campaign.getId() == null) {
            flashSaleCampaignJpa.save(flashSaleCampaignMapper.toEntity(campaign, productVariantJpa::getReferenceById));
            return;
        }
        var entity = flashSaleCampaignJpa.findById(campaign.getId())
                .orElseThrow(() -> new IllegalStateException("Chương trình đã tải không còn tồn tại"));
        flashSaleCampaignMapper.updateEntity(campaign, entity, productVariantJpa::getReferenceById);
        flashSaleCampaignJpa.save(entity);
    }
}
