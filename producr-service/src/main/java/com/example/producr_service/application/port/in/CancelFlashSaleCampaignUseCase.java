package com.example.producr_service.application.port.in;

public interface CancelFlashSaleCampaignUseCase {
    // Gọi hủy lại phải hợp lệ để request lặp không làm thay đổi kết quả nghiệp vụ.
    void cancel(Long campaignId);
}
