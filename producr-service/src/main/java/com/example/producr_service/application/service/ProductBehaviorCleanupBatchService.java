package com.example.producr_service.application.service;

import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ProductBehaviorCleanupBatchService {
    private final ProductBehaviorRepositoryPort productBehaviorRepositoryPort;

    @Transactional
    public int deleteExpiredBefore(Instant cutoff, int batchSize) {
        return productBehaviorRepositoryPort.deleteExpiredBefore(cutoff, batchSize);
    }
}
