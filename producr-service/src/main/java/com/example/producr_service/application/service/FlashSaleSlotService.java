package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.response.FlashSaleOfferView;
import com.example.producr_service.application.port.in.InitializeFlashSaleSlotsUseCase;
import com.example.producr_service.application.port.out.cache.FlashSaleSlotPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FlashSaleSlotService implements InitializeFlashSaleSlotsUseCase {
    private final FlashSaleItemRepositoryPort flashSaleItemRepositoryPort;
    private final FlashSaleSlotPort flashSaleSlotPort;
    private final Clock clock;

    // Số suất tính từ DB có thể dư so với thực tế vì chưa trừ checkout đang xử lý; dư thì saga chặn, thiếu mới từ chối oan.
    @Override
    @Transactional(readOnly = true)
    public int initializeOngoingSlots() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<FlashSaleOfferView> offers = flashSaleItemRepositoryPort.findAllOngoingOffers(now);
        for (FlashSaleOfferView offer : offers) {
            Duration ttl = Duration.between(now, offer.endsAt());
            if (ttl.isNegative() || ttl.isZero()) {
                continue;
            }
            long remaining = Math.max(0L, offer.registeredQuantity() - offer.soldQuantity());
            flashSaleSlotPort.initializeIfAbsent(offer.variantId(), remaining, ttl);
        }
        return offers.size();
    }
}
