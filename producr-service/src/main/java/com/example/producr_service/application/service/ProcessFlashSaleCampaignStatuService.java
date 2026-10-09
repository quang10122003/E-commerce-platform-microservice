package com.example.producr_service.application.service;

import com.example.producr_service.application.port.in.ProcessFlashSaleCampaignStatusUseCase;
import com.example.producr_service.application.dto.response.FlashSaleCampaignStatusTransition;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.registry.FlashSaleCampaignStatuRegistry;
import com.example.producr_service.application.strategy.FlashSaleCampaign.FlashSaleCampaignStatuHandler;
import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.Clock;
import java.time.LocalDateTime;

// Điều phối xử lý trạng thái chương trình sale qua port và registry.
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProcessFlashSaleCampaignStatuService implements ProcessFlashSaleCampaignStatusUseCase {

    FlashSaleCampaignRepositoryPort flashSaleCampaignRepositoryPort;

    FlashSaleCampaignStatuRegistry flashSaleCampaignStatuRegistry;
    Clock clock;

    @Transactional(readOnly = true)
    @Override
    public List<Long> findIdsDueForStatusTransition() {
        return flashSaleCampaignRepositoryPort.findIdsDueForStatusTransition(LocalDateTime.now(clock));
    }

    // Khóa phần chương trình và chỉ lưu trạng thái khi handler thực sự thay đổi.
    @Transactional
    @Override
    public Optional<FlashSaleCampaignStatusTransition> processEvent(Long campaignId) {
        Optional<FlashSaleCampaign> saleCampaignOption = flashSaleCampaignRepositoryPort.findStatusByIdForSkip(campaignId);

        if (saleCampaignOption.isEmpty()) {
            return Optional.empty();
        }
        FlashSaleCampaign saleCampaign = saleCampaignOption.get();
        FlashSaleCampaignStatus previousStatus = saleCampaign.getStatus();
        // Bỏ qua chương trình đã hủy hoặc kết thúc trước khi lấy được khóa.
        if (previousStatus == FlashSaleCampaignStatus.CANCELLED
                || previousStatus == FlashSaleCampaignStatus.DONE) {
            return Optional.empty();
        }
        FlashSaleCampaignStatuHandler handler = flashSaleCampaignStatuRegistry.handle(previousStatus);
        handler.handle(saleCampaign);
        if (saleCampaign.getStatus() == previousStatus) {
            return Optional.empty();
        }
        // chỉ gọi saveStatus k gọi save vì job chỉ lấy saleCampaign ko có item nếu gọi save thì xóa hết item
        flashSaleCampaignRepositoryPort.saveStatus(campaignId, saleCampaign.getStatus());
        return Optional.of(new FlashSaleCampaignStatusTransition(previousStatus, saleCampaign.getStatus()));
    }
}
