package com.example.producr_service.adapter.out.persistence.FlashSaleCampaign;

import com.example.producr_service.adapter.mapper.FlashSaleCampaignMapper;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
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

    // Kiểm tra lịch trùng, bỏ qua chương trình đã hủy hoặc hoàn thành.
    @Override
    public boolean existsOverlapping(LocalDateTime startsAt, LocalDateTime endsAt) {
        return flashSaleCampaignJpa.existsOverlapping(
                startsAt, endsAt, List.of(FlashSaleCampaignStatus.CANCELLED, FlashSaleCampaignStatus.DONE));
    }

    // Tái sử dụng index trang của PageQuery; thứ tự và bộ lọc được áp dụng ngay trong DB.
    @Override
    public PageResponse<FlashSaleCampaign> findAll(PageQuery pageQuery, LocalDateTime from,
                                                 LocalDateTime to, FlashSaleCampaignStatus status,
                                                 LocalDateTime now) {
        var entityPage = flashSaleCampaignJpa.findCampaigns(
                from, to, status == null ? null : status.name(), now,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size())
        );
        return new PageResponse<>(
                entityPage.getContent().stream().map(flashSaleCampaignMapper::toDomain).toList(),
                entityPage.getNumber() + 1,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages()
        );
    }

    // Giữ lock trong transaction của application, rồi chuyển entity thành domain.
    @Override
    public Optional<FlashSaleCampaign> findByIdForUpdate(Long campaignId) {
        return flashSaleCampaignJpa.findByIdForUpdate(campaignId).map(flashSaleCampaignMapper::toDomain);
    }

    // Chuyển domain thành entity và lưu chương trình.
    @Override
    public void save(FlashSaleCampaign campaign) {
        flashSaleCampaignJpa.save(flashSaleCampaignMapper.toEntity(campaign));
    }
}
