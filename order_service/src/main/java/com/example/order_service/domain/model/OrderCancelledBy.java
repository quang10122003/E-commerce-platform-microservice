package com.example.order_service.domain.model;

// Tác nhân hủy đơn; lý do lỗi thanh toán/checkout nằm ở bảng payment/checkout.
public enum OrderCancelledBy {
    USER,
    SHOP,
    // Đối soát thanh toán tự hủy đơn khi VNPay không ghi nhận giao dịch.
    SYSTEM
}
