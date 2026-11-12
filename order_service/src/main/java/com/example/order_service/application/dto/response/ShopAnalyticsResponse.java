package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ShopAnalyticsResponse(OffsetDateTime from, OffsetDateTime to, Overview overview, Overview previousOverview,
                                    BigDecimal saleDiscount, List<DailyPoint> timeline, List<Long> ordersByHour,
                                    List<Slice> byStatus, List<Slice> byPaymentMethod, List<Slice> byPriceType,
                                    List<CancelSlice> cancellations, ProcessingTime processingTime,
                                    List<TopProduct> topProducts) {

    public record Money(long orderCount, BigDecimal amount) {
    }

    // Mốc thời gian riêng từng nhóm: đặt và dự kiến theo ngày đặt, đã về theo ngày giao xong, đã thu theo ngày thanh toán, mất và cần hoàn theo ngày hủy.
    public record Overview(Money placed, Money received, Money held, Money expectedCod, Money awaitingPayment,
                           Money lost, Money refundDue, long buyerCount) {
    }

    public record DailyPoint(OffsetDateTime bucketStart, long placedOrders, long receivedOrders, long cancelledOrders,
                             BigDecimal placedAmount,
                             BigDecimal receivedAmount, BigDecimal paidOnlineAmount, BigDecimal cancelledAmount) {
    }

    public record Slice(String key, long orderCount, BigDecimal amount) {
    }

    public record CancelSlice(OrderCancelledBy cancelledBy, OrderCancelReason reason, long orderCount,
                              BigDecimal amount) {
    }

    // Số phút trung bình của từng chặng; null khi chưa có đơn nào đi hết chặng đó.
    public record ProcessingTime(Long confirmMinutes, Long handoverMinutes, Long deliveryMinutes) {
    }

    public record TopProduct(Long productId, String productName, String imageUrl, long quantity, BigDecimal revenue) {
    }
}
