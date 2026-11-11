package com.example.order_service.application.mapper;

import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.order.ShopOrderView;
import com.example.order_service.application.dto.response.ShopOrderResponse;
import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public final class UserOrderResponseMapper {

    private UserOrderResponseMapper() {
    }

    public static UserOrderResponse toResponse(UserOrderView order, List<UserOrderResponse.Item> items,
                                               LocalDateTime now, ZoneId zone) {
        return toResponse(order, items, now, zone, true);
    }

    // Link VNPay là của người mua nên không trả cho shop.
    public static ShopOrderResponse toShopResponse(ShopOrderView order, List<UserOrderResponse.Item> items,
                                                   LocalDateTime now, ZoneId zone) {
        UserOrderResponse o = toResponse(order.order(), items, now, zone, false);
        return new ShopOrderResponse(o.orderId(), o.checkoutId(), o.status(), o.shop(), o.priceType(), o.totalAmount(),
                o.payment(), o.items(), o.cancelledBy(), o.cancelReason(), o.cancelledAt(), o.confirmedAt(), o.shippedAt(),
                o.deliveredAt(), o.createdAt(), order.buyer());
    }

    private static UserOrderResponse toResponse(UserOrderView order, List<UserOrderResponse.Item> items,
                                                LocalDateTime now, ZoneId zone, boolean exposePaymentUrl) {
        return new UserOrderResponse(order.orderId(), order.checkoutId(), order.status(),
                new UserOrderResponse.Shop(order.shopId(), order.shopName(), order.shopLogoUrl()),
                order.priceType(), order.totalAmount(), toPayment(order, now, zone, exposePaymentUrl), items,
                order.cancelledBy(), order.cancelReason(), toOffset(order.cancelledAt(), zone), toOffset(order.confirmedAt(), zone),
                toOffset(order.shippedAt(), zone), toOffset(order.deliveredAt(), zone), toOffset(order.createdAt(), zone));
    }

    // Link VNPay chỉ trả khi khách còn thanh toán được để FE không mở link đã hết hạn.
    private static UserOrderResponse.Payment toPayment(UserOrderView order, LocalDateTime now, ZoneId zone,
                                                       boolean exposePaymentUrl) {
        boolean payable = exposePaymentUrl && order.paymentStatus() == PaymentStatus.PENDING
                && order.paymentExpiresAt() != null && order.paymentExpiresAt().isAfter(now);
        return new UserOrderResponse.Payment(order.paymentMethod(), order.paymentStatus(),
                payable ? order.paymentUrl() : null, toOffset(order.paymentExpiresAt(), zone),
                toOffset(order.paymentPaidAt(), zone));
    }

    private static OffsetDateTime toOffset(LocalDateTime value, ZoneId zone) {
        return value == null ? null : value.atZone(zone).toOffsetDateTime();
    }
}
