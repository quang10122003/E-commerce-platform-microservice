package com.example.order_service.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Thống kê toàn sàn: cùng bộ số liệu như của một shop, thêm số shop có đơn và các shop dẫn đầu.
public record AdminAnalyticsResponse(ShopAnalyticsResponse summary, long activeShopCount, List<TopShop> topShops) {

    public record TopShop(Long shopId, String shopName, long orderCount, BigDecimal revenue) {
    }
}
