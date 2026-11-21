package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// Các khối dữ liệu dùng chung cho trang tổng quan của shop và admin.
public final class OrderOverviewParts {
    private OrderOverviewParts() {
    }

    // Doanh thu tính trên đơn chưa hủy, theo ngày đặt đơn.
    public record Today(long orderCount, BigDecimal revenue, long cancelledCount,
                        long yesterdayOrderCount, BigDecimal yesterdayRevenue) {
    }

    public record DailyPoint(LocalDate date, long orderCount, long cancelledCount, BigDecimal revenue) {
    }

    public record RecentOrder(Long orderId, Long shopId, String shopName, String recipientName,
                              String firstProductName, int itemCount, BigDecimal totalAmount, OrderStatus status,
                              CheckoutPaymentMethod paymentMethod, OffsetDateTime createdAt) {
    }
}
