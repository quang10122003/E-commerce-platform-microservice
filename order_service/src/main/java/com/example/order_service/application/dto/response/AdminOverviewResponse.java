package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.OrderStatus;

import java.util.List;
import java.util.Map;

// Tổng quan toàn sàn cho admin: hôm nay, đơn theo trạng thái hiện tại, 14 ngày gần nhất, top shop 7 ngày và đơn mới.
public record AdminOverviewResponse(OrderOverviewParts.Today today, Map<OrderStatus, Long> ordersByStatus,
                                    List<OrderOverviewParts.DailyPoint> last14Days,
                                    List<AdminAnalyticsResponse.TopShop> topShopsLast7Days,
                                    List<OrderOverviewParts.RecentOrder> recentOrders) {
}
