package com.example.producr_service.adapter.in.scheduler;

import com.example.producr_service.application.port.in.InitializeFlashSaleSlotsUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FlashSaleSlotScheduler {

    InitializeFlashSaleSlotsUseCase initializeFlashSaleSlotsUseCase;

    // Chạy định kỳ để bộ đếm có ngay khi chiến dịch mở bán và tự dựng lại nếu Redis bị xóa hoặc khởi động lại.
    @Scheduled(fixedDelayString = "${app.flash-sale.slot-init-fixed-delay-ms:5000}")
    public void initializeSlots() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            int count = initializeFlashSaleSlotsUseCase.initializeOngoingSlots();
            log.debug("Đã kiểm tra bộ đếm suất flash sale: số item={}", count);
        } catch (Exception exception) {
            log.error("Khởi tạo bộ đếm suất flash sale thất bại", exception);
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }
}
