package com.example.producr_service.application.port.out.cache;

import java.time.Duration;

public interface FlashSaleSlotPort {
    // Chỉ tạo khi chưa có để không ghi đè số suất Order đang giữ cho checkout chưa xong.
    void initializeIfAbsent(Long variantId, long remaining, Duration ttl);

    // Bộ đếm đã hết hạn cùng chiến dịch thì bỏ qua, không tạo lại.
    void release(Long variantId, int quantity);
}
