package com.example.producr_service.application.port.in;

public interface CancelFlashSaleCampaignUseCase {
    // Hủy chương trình, cho phép gọi lại khi đã hủy.
    void cancel(Long campaignId);
}
