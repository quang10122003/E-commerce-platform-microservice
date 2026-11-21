package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Tổng quan Kênh Người Bán: hôm nay, đơn theo trạng thái hiện tại, 7 ngày gần nhất, đánh giá và đơn mới.
public record ShopOverviewResponse(OrderOverviewParts.Today today, Map<OrderStatus, Long> ordersByStatus,
                                   List<OrderOverviewParts.DailyPoint> last7Days, Reviews reviews,
                                   List<OrderOverviewParts.RecentOrder> recentOrders) {

    // averageRating null khi shop chưa có đánh giá nào.
    public record Reviews(long totalReviews, BigDecimal averageRating, long newReviewsLast7Days) {
    }
}
