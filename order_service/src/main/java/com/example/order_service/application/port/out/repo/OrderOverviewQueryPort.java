package com.example.order_service.application.port.out.repo;

import com.example.order_service.application.dto.order.RecentOrderView;
import com.example.order_service.domain.model.OrderStatus;

import java.util.List;
import java.util.Map;

public interface OrderOverviewQueryPort {
    // shopId null nghĩa là toàn sàn; trạng thái không có đơn nào thì không có trong map.
    Map<OrderStatus, Long> countOrdersByStatus(Long shopId);

    // Đơn mới đặt nhất trước.
    List<RecentOrderView> findRecentOrders(Long shopId, int limit);
}
