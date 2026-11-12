package com.example.order_service.application.dto.analytics;

import com.example.order_service.application.dto.response.ShopAnalyticsResponse;

import java.math.BigDecimal;

// Cộng dồn số đơn và số tiền của một nhóm trong lúc duyệt danh sách đơn.
public class MoneyTally {
    private long count;
    private BigDecimal amount = BigDecimal.ZERO;

    public void add(BigDecimal value) {
        count++;
        amount = amount.add(value);
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ShopAnalyticsResponse.Money toMoney() {
        return new ShopAnalyticsResponse.Money(count, amount);
    }
}
