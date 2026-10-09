package com.example.producr_service.application.port.in;



import java.util.List;
import java.util.Optional;
import com.example.producr_service.application.dto.response.FlashSaleCampaignStatusTransition;

public interface ProcessFlashSaleCampaignStatusUseCase {
    // Lấy ID các chương trình đến hạn chuyển trạng thái.
    List<Long> findIdsDueForStatusTransition();

    // Trả trạng thái trước/sau khi thay đổi; rỗng nếu không lấy được khóa hoặc không đổi trạng thái.
    Optional<FlashSaleCampaignStatusTransition> processEvent(Long campaignId);
}
