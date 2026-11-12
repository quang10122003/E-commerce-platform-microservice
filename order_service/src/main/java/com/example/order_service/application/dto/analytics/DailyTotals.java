package com.example.order_service.application.dto.analytics;

import com.example.order_service.application.dto.response.ShopAnalyticsResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Cộng dồn các dòng tiền của một ngày, mỗi dòng tính theo mốc thời gian riêng.
public class DailyTotals {
    private long placedOrders;
    private long receivedOrders;
    private long cancelledOrders;
    private BigDecimal placed = BigDecimal.ZERO;
    private BigDecimal received = BigDecimal.ZERO;
    private BigDecimal paidOnline = BigDecimal.ZERO;
    private BigDecimal cancelled = BigDecimal.ZERO;

    public void addPlaced(BigDecimal amount) {
        placedOrders++;
        placed = placed.add(amount);
    }

    public void addReceived(BigDecimal amount) {
        receivedOrders++;
        received = received.add(amount);
    }

    public void addPaidOnline(BigDecimal amount) {
        paidOnline = paidOnline.add(amount);
    }

    public void addCancelled(BigDecimal amount) {
        cancelledOrders++;
        cancelled = cancelled.add(amount);
    }

    public ShopAnalyticsResponse.DailyPoint toPoint(OffsetDateTime bucketStart) {
        return new ShopAnalyticsResponse.DailyPoint(bucketStart, placedOrders, receivedOrders, cancelledOrders, placed, received,
                paidOnline, cancelled);
    }
}
