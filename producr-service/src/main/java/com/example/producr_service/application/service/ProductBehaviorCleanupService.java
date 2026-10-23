package com.example.producr_service.application.service;

import com.example.producr_service.application.port.in.CleanupExpiredProductBehaviorsUseCase;
import com.example.producr_service.domain.model.UserProductBehavior;
import java.time.Clock;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductBehaviorCleanupService implements CleanupExpiredProductBehaviorsUseCase {
    private final ProductBehaviorCleanupBatchService batchService;
    private final Clock clock;
    private final int batchSize;

    public ProductBehaviorCleanupService(ProductBehaviorCleanupBatchService batchService, Clock clock, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Kích thước lô dọn hành vi phải lớn hơn 0");
        }
        this.batchService = batchService;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    @Override
    public long cleanupExpiredBehaviors() {
        Instant cutoff = clock.instant().minus(UserProductBehavior.RETENTION);
        long deletedTotal = 0;
        int deleted;
        do {
            // để hàm xóa data ở file khác xóa theo lô  mỗi lô được commit độc lập qua transaction proxy.
            deleted = batchService.deleteExpiredBefore(cutoff, batchSize);
            deletedTotal += deleted;
        } while (deleted == batchSize);
        log.info("Dọn hành vi hết hạn hoàn tất: cutoff={}, deleted={}", cutoff, deletedTotal);
        return deletedTotal;
    }
}
