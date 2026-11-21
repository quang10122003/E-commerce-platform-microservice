package com.example.order_service.adapter.out.persistence.projection;

// Đơn của shop kèm snapshot người mua lấy từ checkout tại thời điểm đặt hàng.
public interface ShopOrderProjection extends UserOrderProjection {
    String getRecipientName();
    String getBuyerEmail();
    String getShippingAddress();
    String getPhone();
    String getNote();
}
