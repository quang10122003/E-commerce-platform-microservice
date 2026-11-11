package com.example.order_service.domain.model;

// Lý do do người hủy chọn; SYSTEM hủy thì để null vì lỗi thanh toán nằm ở bảng payment.
public enum OrderCancelReason {
    CHANGE_MIND(OrderCancelledBy.USER),
    WRONG_ADDRESS(OrderCancelledBy.USER),
    FOUND_CHEAPER(OrderCancelledBy.USER),
    DUPLICATE_ORDER(OrderCancelledBy.USER),
    OUT_OF_STOCK(OrderCancelledBy.SHOP),
    CANNOT_DELIVER(OrderCancelledBy.SHOP),
    BUYER_UNREACHABLE(OrderCancelledBy.SHOP),
    PRODUCT_INFO_ERROR(OrderCancelledBy.SHOP),
    // Dùng chung cho cả user và shop.
    OTHER(null);

    private final OrderCancelledBy actor;

    OrderCancelReason(OrderCancelledBy actor) {
        this.actor = actor;
    }

    // User và shop chỉ được chọn lý do của mình, tránh user gửi "hết hàng" hoặc shop gửi "đổi ý".
    public boolean allowedFor(OrderCancelledBy cancelledBy) {
        return cancelledBy != OrderCancelledBy.SYSTEM && (actor == null || actor == cancelledBy);
    }
}
