package com.example.order_service.application.service;

import com.example.order_service.application.dto.order.RecentOrderView;
import com.example.order_service.application.dto.order.ShopAnalyticsOrderView;
import com.example.order_service.application.dto.order.UserOrderItemView;
import com.example.order_service.application.dto.response.AdminOverviewResponse;
import com.example.order_service.application.dto.response.OrderOverviewParts;
import com.example.order_service.application.dto.response.ShopOverviewResponse;
import com.example.order_service.application.port.in.GetAdminOverviewUseCase;
import com.example.order_service.application.port.in.GetShopOverviewUseCase;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.repo.OrderOverviewQueryPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.ProductReviewRepoPort;
import com.example.order_service.application.port.out.repo.ShopAnalyticsQueryPort;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.RatingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderOverviewService implements GetShopOverviewUseCase, GetAdminOverviewUseCase {
    private static final int SHOP_DAYS = 7;
    private static final int ADMIN_DAYS = 14;
    private static final int TOP_SHOP_DAYS = 7;
    private static final int RECENT_ORDER_LIMIT = 6;
    private static final int TOP_SHOP_LIMIT = 5;
    private static final int NEW_REVIEW_DAYS = 7;

    private final CurrentUserPort currentUserPort;
    private final ShopAnalyticsQueryPort analyticsQueryPort;
    private final OrderOverviewQueryPort overviewQueryPort;
    private final OrderRepoPort orderRepoPort;
    private final ProductReviewRepoPort productReviewRepoPort;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ShopOverviewResponse getShopOverview() {
        Long shopId = currentUserPort.getInfo().userId();
        List<OrderOverviewParts.DailyPoint> days = dailyPoints(shopId, SHOP_DAYS);
        RatingSummary rating = productReviewRepoPort.findShopRatingSummary(shopId);
        BigDecimal averageRating = rating.reviewCount() == 0 ? null
                : BigDecimal.valueOf(rating.ratingSum()).divide(BigDecimal.valueOf(rating.reviewCount()), 2, RoundingMode.HALF_UP);
        long newReviews = productReviewRepoPort.countShopReviewsSince(shopId,
                LocalDateTime.now(clock).minusDays(NEW_REVIEW_DAYS));
        return new ShopOverviewResponse(today(days), ordersByStatus(shopId), days,
                new ShopOverviewResponse.Reviews(rating.reviewCount(), averageRating, newReviews), recentOrders(shopId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse getAdminOverview() {
        List<OrderOverviewParts.DailyPoint> days = dailyPoints(null, ADMIN_DAYS);
        LocalDateTime topShopStart = LocalDate.now(clock).minusDays(TOP_SHOP_DAYS - 1L).atStartOfDay();
        return new AdminOverviewResponse(today(days), ordersByStatus(null), days,
                analyticsQueryPort.findTopShops(topShopStart, LocalDateTime.now(clock), TOP_SHOP_LIMIT),
                recentOrders(null));
    }

    // Gom theo ngày đặt đơn; đơn đã hủy vẫn tính vào số đơn nhưng không tính vào doanh thu.
    private List<OrderOverviewParts.DailyPoint> dailyPoints(Long shopId, int dayCount) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime start = today.minusDays(dayCount - 1L).atStartOfDay();
        Map<LocalDate, List<ShopAnalyticsOrderView>> byDate = analyticsQueryPort
                .findOrdersTouching(shopId, start, LocalDateTime.now(clock)).stream()
                .filter((ShopAnalyticsOrderView order) -> !order.createdAt().isBefore(start))
                .collect(Collectors.groupingBy((ShopAnalyticsOrderView order) -> order.createdAt().toLocalDate()));
        List<OrderOverviewParts.DailyPoint> points = new ArrayList<>();
        for (LocalDate date = start.toLocalDate(); !date.isAfter(today); date = date.plusDays(1)) {
            List<ShopAnalyticsOrderView> orders = byDate.getOrDefault(date, List.of());
            long cancelled = orders.stream().filter((ShopAnalyticsOrderView order) -> order.status() == OrderStatus.CANCEL).count();
            points.add(new OrderOverviewParts.DailyPoint(date, orders.size(), cancelled, revenue(orders)));
        }
        return points;
    }

    private OrderOverviewParts.Today today(List<OrderOverviewParts.DailyPoint> days) {
        OrderOverviewParts.DailyPoint today = days.get(days.size() - 1);
        OrderOverviewParts.DailyPoint yesterday = days.get(days.size() - 2);
        return new OrderOverviewParts.Today(today.orderCount(), today.revenue(), today.cancelledCount(),
                yesterday.orderCount(), yesterday.revenue());
    }

    // Đủ mọi trạng thái, trạng thái không có đơn trả về 0 để FE không phải tự kiểm tra thiếu khóa.
    private Map<OrderStatus, Long> ordersByStatus(Long shopId) {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0L);
        }
        counts.putAll(overviewQueryPort.countOrdersByStatus(shopId));
        return counts;
    }

    private List<OrderOverviewParts.RecentOrder> recentOrders(Long shopId) {
        List<RecentOrderView> orders = overviewQueryPort.findRecentOrders(shopId, RECENT_ORDER_LIMIT);
        Map<Long, List<UserOrderItemView>> itemsByOrder = orders.isEmpty() ? Map.of()
                : orderRepoPort.findUserOrderItems(orders.stream().map(RecentOrderView::orderId).toList()).stream()
                .collect(Collectors.groupingBy(UserOrderItemView::orderId));
        return orders.stream().map((RecentOrderView order) -> {
            List<UserOrderItemView> items = itemsByOrder.getOrDefault(order.orderId(), List.of());
            return new OrderOverviewParts.RecentOrder(order.orderId(), order.shopId(), order.shopName(),
                    order.recipientName(), items.isEmpty() ? null : items.get(0).item().productName(), items.size(),
                    order.totalAmount(), order.status(), order.paymentMethod(),
                    order.createdAt().atZone(clock.getZone()).toOffsetDateTime());
        }).toList();
    }

    private BigDecimal revenue(List<ShopAnalyticsOrderView> orders) {
        return orders.stream().filter((ShopAnalyticsOrderView order) -> order.status() != OrderStatus.CANCEL)
                .map(ShopAnalyticsOrderView::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
