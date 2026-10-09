package com.example.producr_service.adapter.in.scheduler;

import com.example.producr_service.application.port.in.ProcessFlashSaleCampaignStatusUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
@Slf4j
@Component
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FlashSaleCampaignStatusScheduler {

    ProcessFlashSaleCampaignStatusUseCase processFlashSaleCampaignStatusUseCase;


    @Scheduled(fixedDelayString = "${app.flash-sale.status-fixed-delay-ms:10000}")
    public void processPendingEvents() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<Long> campaignIds = processFlashSaleCampaignStatusUseCase.findIdsDueForStatusTransition();
            log.debug("Bắt đầu xử lý trạng thái Flash Sale: số lượng={}", campaignIds.size());
            campaignIds.forEach(this::processEvent);
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }
    // Cô lập lỗi từng chương trình để các ID còn lại vẫn được xử lý.
    private void processEvent(Long campaignId) {
        try {
            processFlashSaleCampaignStatusUseCase.processEvent(campaignId).ifPresent(transition ->
                    log.info("Đã cập nhật trạng thái chương trình sale: id={}, từ={} → đến={}",
                            campaignId, transition.previousStatus(), transition.currentStatus()));
        } catch (Exception exception) {
            log.error("Cập nhật trạng thái chương trình sale thất bại: id={}", campaignId, exception);
        }
    }
}
