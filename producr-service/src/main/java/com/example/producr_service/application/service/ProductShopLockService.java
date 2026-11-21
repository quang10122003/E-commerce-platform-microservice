package com.example.producr_service.application.service;

import com.example.common.exception.CacheAccessException;
import com.example.producr_service.application.constant.ProductCacheKeys;
import com.example.producr_service.application.port.out.cache.CachePort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ProductShopLockService {
    private final ProductRepositoryPort productRepositoryPort;
    private final OutboxPort outboxPort;
    private final CachePort cachePort;
    private final Clock clock;

    // Cắt về micro giây cho khớp DATETIME(6), nếu không bước đọc lại ID theo mốc thời gian sẽ không khớp.
    @Transactional
    public void syncShopLock(Long shopId, boolean locked, LocalDateTime occurredAt) {
        List<Long> productIds = productRepositoryPort.updateShopLock(shopId, locked,
                occurredAt.truncatedTo(ChronoUnit.MICROS));
        productIds.forEach(productId -> ProductSyncEvents.saveSearchIndexSync(outboxPort, clock, productId));
        if (!productIds.isEmpty()) {
            evictTopSellingCache();
        }
        log.info("Đồng bộ trạng thái khóa shop: shop_id={}, locked={}, products={}", shopId, locked, productIds.size());
    }

    private void evictTopSellingCache() {
        try {
            cachePort.delete(ProductCacheKeys.TOP_SELLING_PRODUCTS);
        } catch (CacheAccessException exception) {
            log.warn("Không thể xóa cache sản phẩm bán chạy sau khi đổi trạng thái khóa shop");
        }
    }
}
