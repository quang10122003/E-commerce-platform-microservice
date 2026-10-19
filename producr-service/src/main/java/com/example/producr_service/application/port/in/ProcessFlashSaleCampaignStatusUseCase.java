package com.example.producr_service.application.port.in;



import java.util.List;
import java.util.Optional;
import com.example.producr_service.application.dto.response.FlashSaleCampaignStatusTransition;

public interface ProcessFlashSaleCampaignStatusUseCase {
    List<Long> findIdsDueForStatusTransition();

    // Kết quả rỗng cũng có thể do không lấy được khóa, không nhất thiết thiếu chương trình.
    Optional<FlashSaleCampaignStatusTransition> processEvent(Long campaignId);
}
