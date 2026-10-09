package com.example.producr_service.application.strategy.FlashSaleCampaign;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleCampaignDraftHandler implements FlashSaleCampaignStatuHandler {

    Clock clock;

    @Override
    public FlashSaleCampaignStatus eventType() {
        return FlashSaleCampaignStatus.DRAFT;
    }

    @Override
    public void handle(FlashSaleCampaign flashSaleCampaign) {

        LocalDateTime now = LocalDateTime.now(clock);
        // Đã đến hoặc qua thời điểm bắt đầu sale.
        if (!now.isBefore(flashSaleCampaign.getStartsAt())) {
            flashSaleCampaign.updateStatus(FlashSaleCampaignStatus.PUBLISHED);
            // Kết thúc ngay lịch đã hết hạn, không công bố sale quá hạn giữa hai lượt job.
            if (!now.isBefore(flashSaleCampaign.getEndsAt())) {
                flashSaleCampaign.updateStatus(FlashSaleCampaignStatus.DONE);
            }
        }
    }
}
