package com.example.order_service.application.port.out.repo;

import com.example.order_service.application.dto.order.ShopAnalyticsOrderView;
import com.example.order_service.application.dto.response.AdminAnalyticsResponse;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ShopAnalyticsQueryPort {
    // Ở ba truy vấn theo shop, shopId null nghĩa là thống kê toàn sàn cho admin.
    // Đơn có ít nhất một mốc đặt, thanh toán, giao xong hoặc hủy nằm trong khoảng.
    List<ShopAnalyticsOrderView> findOrdersTouching(Long shopId, LocalDateTime from, LocalDateTime to);

    List<ShopAnalyticsResponse.TopProduct> findTopProducts(Long shopId, LocalDateTime from, LocalDateTime to, int limit);

    BigDecimal sumSaleDiscount(Long shopId, LocalDateTime from, LocalDateTime to);

    List<AdminAnalyticsResponse.TopShop> findTopShops(LocalDateTime from, LocalDateTime to, int limit);

    long countActiveShops(LocalDateTime from, LocalDateTime to);
}
