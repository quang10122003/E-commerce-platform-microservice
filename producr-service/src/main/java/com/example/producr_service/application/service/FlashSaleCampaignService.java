package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.producr_service.application.dto.request.FlashSaleCampaignFilter;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.FlashSaleCampaignResponse;
import com.example.producr_service.application.port.in.GetFlashSaleCampaignsUseCase;
import com.example.producr_service.application.dto.request.CreateFlashSaleCampaignRequest;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.CreateFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.domain.model.FlashSaleCampaign;
import org.springframework.transaction.annotation.Transactional;

import com.example.producr_service.application.port.in.CancelFlashSaleCampaignUseCase;
import java.time.Clock;
import java.time.LocalDateTime;

public class FlashSaleCampaignService implements CreateFlashSaleCampaignUseCase, GetFlashSaleCampaignsUseCase, CancelFlashSaleCampaignUseCase {
    private final CurrentUserPort currentUserPort;
    private final FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort;
    private final Clock clock;

    public FlashSaleCampaignService(CurrentUserPort currentUserPort,
                                    FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort,
                                    Clock clock) {
        this.currentUserPort = currentUserPort;
        this.flashSaleCampaignRepositoryPort = flashSaleCampaignRepositoryPort;
        this.clock = clock;
    }

    // Khóa trước khi kiểm tra và hủy để phối hợp với shared lock của luồng mua.
    @Override
    @Transactional
    public void cancel(Long campaignId) {
        if (campaignId == null ) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_CAMPAIGN_ID);
        }
        FlashSaleCampaign campaign = flashSaleCampaignRepositoryPort.findByIdForUpdate(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));
        if (campaign.cancel(LocalDateTime.now(clock))) {
            flashSaleCampaignRepositoryPort.save(campaign);
        }
    }

    // Đọc trang chương trình và trả thời gian có offset theo múi giờ Việt Nam.
    @Override
    @Transactional(readOnly = true)
    public PageResponse<FlashSaleCampaignResponse> getCampaigns(FlashSaleCampaignFilter filter) {
        if (filter.page() < 1 || filter.size() < 10 || filter.size() > 30) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_PAGINATION);
        }
        // Khoảng lọc phải có đủ hai đầu form phỉa trc to
        if ((filter.from() == null) != (filter.to() == null)
                || (filter.from() != null && filter.from().toInstant().isAfter(filter.to().toInstant()))) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_TIME_FILTER);
        }
        LocalDateTime from = filter.from() == null ? null
                : filter.from().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        LocalDateTime to = filter.to() == null ? null
                : filter.to().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        PageResponse<FlashSaleCampaign> campaigns = flashSaleCampaignRepositoryPort.findAll(
                new PageQuery(filter.page(), filter.size()), from, to, filter.status(),
                LocalDateTime.now(clock));
        return new PageResponse<>(
                campaigns.items().stream()
                        .map(campaign -> FlashSaleCampaignResponse.from(campaign, clock.getZone()))
                        .toList(),
                campaigns.page(),
                campaigns.pageSize(),
                campaigns.totalItems(),
                campaigns.totalPages()
        );
    }

    // Tạo DRAFT cho Shop đăng ký; lịch sale không được trùng DRAFT hoặc PUBLISHED.
    @Override
    @Transactional
    public void create(CreateFlashSaleCampaignRequest request) {
        Long createdBy = currentUserPort.getUserInfo().userId();
        LocalDateTime now = LocalDateTime.now(clock);
        // Quy đổi cùng thời điểm về múi giờ Việt Nam đã cấu hình trong Clock.
        LocalDateTime registrationDeadline = request.registrationDeadline()
                .atZoneSameInstant(clock.getZone()).toLocalDateTime();
        LocalDateTime startsAt = request.startsAt()
                .atZoneSameInstant(clock.getZone()).toLocalDateTime();
        LocalDateTime endsAt = request.endsAt()
                .atZoneSameInstant(clock.getZone()).toLocalDateTime();
        FlashSaleCampaign campaign = FlashSaleCampaign.create(
                request.name(),
                request.description(),
                registrationDeadline,
                startsAt,
                endsAt,
                createdBy,
                now
        );

        // ko cho tạo chương trình trùng lịch nhau
        if (flashSaleCampaignRepositoryPort.existsOverlapping(
                campaign.getStartsAt(), campaign.getEndsAt())) {
            throw new BusinessException(ProductError.FLASH_SALE_SCHEDULE_CONFLICT);
        }

        flashSaleCampaignRepositoryPort.save(campaign);
    }
}
