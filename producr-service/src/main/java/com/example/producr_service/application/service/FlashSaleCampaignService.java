package com.example.producr_service.application.service;

import com.example.producr_service.application.port.in.GetCurrentFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetAdminFlashSaleCampaignDetailUseCase;
import com.example.producr_service.application.dto.response.CurrentFlashSaleCampaignResponse;
import com.example.producr_service.application.dto.response.AdminFlashSaleCampaignDetailResponse;
import com.example.producr_service.domain.model.FlashSaleItemStatus;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.producr_service.application.dto.request.FlashSaleCampaignFilter;
import com.example.producr_service.application.dto.request.ShopCompletedFlashSaleCampaignFilter;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.FlashSaleCampaignResponse;
import com.example.producr_service.application.dto.response.CompletedFlashSaleCampaignView;
import com.example.producr_service.application.dto.response.ShopCompletedFlashSaleCampaignResponse;
import com.example.producr_service.application.port.in.GetShopCompletedFlashSaleCampaignsUseCase;
import com.example.producr_service.application.port.in.GetFlashSaleCampaignsUseCase;
import com.example.producr_service.application.dto.request.CreateFlashSaleCampaignRequest;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.CreateFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.domain.model.FlashSaleCampaign;
import org.springframework.transaction.annotation.Transactional;

import com.example.producr_service.application.port.in.CancelFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignsUseCase;
import com.example.producr_service.application.dto.request.ShopFlashSaleCampaignFilter;
import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignResponse;
import com.example.producr_service.application.dto.response.FlashSaleCampaignView;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import java.time.OffsetDateTime;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignDetailUseCase;
import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignDetailResponse;
import java.util.Optional;
import java.util.List;
import com.example.producr_service.application.port.in.ProcessFlashSaleCampaignStatusUseCase;
import com.example.producr_service.application.dto.response.FlashSaleCampaignStatusTransition;
import com.example.producr_service.application.registry.FlashSaleCampaignStatuRegistry;
import com.example.producr_service.application.strategy.FlashSaleCampaign.FlashSaleCampaignStatuHandler;
import java.time.Clock;
import java.time.LocalDateTime;

public class FlashSaleCampaignService implements GetAdminFlashSaleCampaignDetailUseCase, GetCurrentFlashSaleCampaignUseCase, CreateFlashSaleCampaignUseCase, GetFlashSaleCampaignsUseCase, CancelFlashSaleCampaignUseCase, GetShopFlashSaleCampaignsUseCase, GetShopFlashSaleCampaignDetailUseCase, GetShopCompletedFlashSaleCampaignsUseCase,
        ProcessFlashSaleCampaignStatusUseCase {
    private final CurrentUserPort currentUserPort;
    private final FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort;
    private final Clock clock;
    private final ShopAccessService shopAccessService;
    private final FlashSaleCampaignStatuRegistry flashSaleCampaignStatuRegistry;

    public FlashSaleCampaignService(CurrentUserPort currentUserPort,
                                    FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort,
                                    Clock clock, ShopAccessService shopAccessService,
                                    FlashSaleCampaignStatuRegistry flashSaleCampaignStatuRegistry) {
        this.currentUserPort = currentUserPort;
        this.flashSaleCampaignRepositoryPort = flashSaleCampaignRepositoryPort;
        this.clock = clock;
        this.shopAccessService = shopAccessService;
        this.flashSaleCampaignStatuRegistry = flashSaleCampaignStatuRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFlashSaleCampaignDetailResponse getCampaignDetail(Long campaignId, FlashSaleItemStatus status, String keyword) {
        if (campaignId == null || campaignId <= 0) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_CAMPAIGN_ID);
        }
        FlashSaleCampaignView campaign = flashSaleCampaignRepositoryPort.findViewById(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));
        return AdminFlashSaleCampaignDetailResponse.from(campaign,
                flashSaleCampaignRepositoryPort.findCurrentItems(campaignId, status, keyword), clock.getZone());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CurrentFlashSaleCampaignResponse> getCurrentCampaign(FlashSaleItemStatus status, String keyword) {
        return flashSaleCampaignRepositoryPort.findCurrent(LocalDateTime.now(clock))
                .map(campaign -> CurrentFlashSaleCampaignResponse.from(campaign,
                        flashSaleCampaignRepositoryPort.findCurrentItems(campaign.id(), status, keyword), clock.getZone()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShopCompletedFlashSaleCampaignResponse> getShopCompletedCampaigns(
            ShopCompletedFlashSaleCampaignFilter filter) {
        validateCampaignFilter(filter.page(), filter.size(), filter.from(), filter.to());

        Long shopId = shopAccessService.requireSellerAccess().userId();
        String searchKeyword = filter.keyword() == null || filter.keyword().isBlank() ? null : filter.keyword().trim();
        LocalDateTime from = filter.from() == null ? null
                : filter.from().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        LocalDateTime to = filter.to() == null ? null
                : filter.to().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        PageResponse<CompletedFlashSaleCampaignView> campaigns = flashSaleCampaignRepositoryPort.findCompletedByShopId(
                shopId, new PageQuery(filter.page(), filter.size()), searchKeyword, from, to);
        return new PageResponse<>(campaigns.items().stream()
                .map(campaign -> ShopCompletedFlashSaleCampaignResponse.from(campaign, clock.getZone())).toList(),
                campaigns.page(), campaigns.pageSize(), campaigns.totalItems(), campaigns.totalPages());
    }

    // Đọc chi tiết không phụ thuộc trạng thái; điều kiện đăng ký được kiểm tra ở luồng ghi.
    @Override
    @Transactional(readOnly = true)
    public ShopFlashSaleCampaignDetailResponse getShopCampaignDetail(Long campaignId) {
        shopAccessService.requireSellerAccess();
        if (campaignId == null || campaignId <= 0) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_CAMPAIGN_ID);
        }
        FlashSaleCampaignView campaign = flashSaleCampaignRepositoryPort.findViewById(campaignId)
                .orElseThrow(() -> new BusinessException(ProductError.FLASH_SALE_CAMPAIGN_NOT_FOUND));
        return new ShopFlashSaleCampaignDetailResponse(campaign.name(),
                campaign.registrationDeadline().atZone(clock.getZone()).toOffsetDateTime());
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FlashSaleCampaignResponse> getCampaigns(FlashSaleCampaignFilter filter) {
        validateCampaignFilter(filter.page(), filter.size(), filter.from(), filter.to());
        LocalDateTime from = filter.from() == null ? null
                : filter.from().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        LocalDateTime to = filter.to() == null ? null
                : filter.to().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        PageResponse<FlashSaleCampaignView> campaigns = flashSaleCampaignRepositoryPort.findAll(
                new PageQuery(filter.page(), filter.size()), from, to, filter.status(),
                LocalDateTime.now(clock), null,
                filter.keyword() == null || filter.keyword().isBlank() ? null : filter.keyword().trim());
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

    // Filter, sắp xếp và quyền đăng ký phải dùng cùng now để tránh kết quả mâu thuẫn sát hạn.
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShopFlashSaleCampaignResponse> getShopCampaigns(ShopFlashSaleCampaignFilter filter) {
        shopAccessService.requireSellerAccess();
        validateCampaignFilter(filter.page(), filter.size(), filter.from(), filter.to());
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime from = filter.from() == null ? null
                : filter.from().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        LocalDateTime to = filter.to() == null ? null
                : filter.to().atZoneSameInstant(clock.getZone()).toLocalDateTime();
        PageResponse<FlashSaleCampaignView> campaigns = flashSaleCampaignRepositoryPort.findAll(
                new PageQuery(filter.page(), filter.size()), from, to, FlashSaleCampaignStatus.DRAFT,
                now, filter.registrationOpen(), null);
        return new PageResponse<>(campaigns.items().stream()
                .map(campaign -> ShopFlashSaleCampaignResponse.from(campaign, clock.getZone(), now)).toList(),
                campaigns.page(), campaigns.pageSize(), campaigns.totalItems(), campaigns.totalPages());
    }

    private void validateCampaignFilter(int page, int size, OffsetDateTime from, OffsetDateTime to) {
        if (page < 1 || size < 10 || size > 30) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_PAGINATION);
        }
        if (from != null && to != null && from.toInstant().isAfter(to.toInstant())) {
            throw new BusinessException(ProductError.INVALID_FLASH_SALE_TIME_FILTER);
        }
    }

    @Override
    @Transactional
    public void create(CreateFlashSaleCampaignRequest request) {
        Long createdBy = currentUserPort.getUserInfo().userId();
        LocalDateTime now = LocalDateTime.now(clock);
        // Offset đầu vào có thể khác múi giờ lưu trữ; phải giữ nguyên thời điểm khi quy đổi.
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

        if (flashSaleCampaignRepositoryPort.existsOverlapping(
                campaign.getStartsAt(), campaign.getEndsAt())) {
            throw new BusinessException(ProductError.FLASH_SALE_SCHEDULE_CONFLICT);
        }

        flashSaleCampaignRepositoryPort.save(campaign);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findIdsDueForStatusTransition() {
        return flashSaleCampaignRepositoryPort.findIdsDueForStatusTransition(LocalDateTime.now(clock));
    }

    @Transactional
    @Override
    public Optional<FlashSaleCampaignStatusTransition> processEvent(Long campaignId) {
        Optional<FlashSaleCampaign> saleCampaignOption = flashSaleCampaignRepositoryPort.findStatusByIdForSkip(campaignId);

        if (saleCampaignOption.isEmpty()) {
            return Optional.empty();
        }
        FlashSaleCampaign saleCampaign = saleCampaignOption.get();
        FlashSaleCampaignStatus previousStatus = saleCampaign.getStatus();
        if (previousStatus == FlashSaleCampaignStatus.CANCELLED
                || previousStatus == FlashSaleCampaignStatus.DONE) {
            return Optional.empty();
        }
        FlashSaleCampaignStatuHandler handler = flashSaleCampaignStatuRegistry.handle(previousStatus);
        handler.handle(saleCampaign);
        if (saleCampaign.getStatus() == previousStatus) {
            return Optional.empty();
        }
        // Chỉ gọi saveStatus vì aggregate không có items; gọi save có thể xóa toàn bộ đăng ký.
        flashSaleCampaignRepositoryPort.saveStatus(campaignId, saleCampaign.getStatus());
        return Optional.of(new FlashSaleCampaignStatusTransition(previousStatus, saleCampaign.getStatus()));
    }
}
