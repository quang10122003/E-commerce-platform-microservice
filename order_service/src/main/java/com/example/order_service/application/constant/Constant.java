package com.example.order_service.application.constant;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Constant {
    public static final String AUTH_EVENTS_TOPIC = "auth-service.events";
    public static final String ORDER_EVENTS_TOPIC = "order-service.events";
    public static final String CART = "Cart";
    public static final int OUTBOX_BATCH_SIZE = 100;
    public static final int OUTBOX_PUBLISH_TIMEOUT_SECONDS = 20;
    public static final int ORDER_EVENTS_PARTITIONS = 3;
    public static final int ORDER_EVENTS_REPLICAS = 1;
    public static final String PRODUCT_EVENTS_TOPIC = "product-service.events";
    public static final String DEAD_LETTER_TOPIC = "order-service.DLT";
    public static final String CHECKOUT = "Checkout";
    public static final String ORDER = "Order";
    public static final String REVIEW = "ProductReview";
    public static final String REVIEW_BUCKET = "reviews";
    public static final String STORAGE_OBJECT_PATH = "/storage/v1/object/";
    public static final String STORAGE_PUBLIC_OBJECT_PATH = STORAGE_OBJECT_PATH + "public/";
    public static final String CHECKOUT_INVALID_REQUEST = "CHECKOUT_INVALID_REQUEST";
    public static final String CHECKOUT_ORDER_CREATION_FAILED = "CHECKOUT_ORDER_CREATION_FAILED";
    public static final String CHECKOUT_PAYMENT_FAILED = "CHECKOUT_PAYMENT_FAILED";
    public static final String CHECKOUT_DEFAULT_CANCEL_REASON = "Không thể tiếp tục checkout";
    public static final String CHECKOUT_INVALID_REQUEST_REASON = "Thông tin đơn hàng không hợp lệ, vui lòng thử lại.";
    public static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final String VNPAY_SECURE_HASH = "vnp_SecureHash";
    public static final String VNPAY_SECURE_HASH_TYPE = "vnp_SecureHashType";
    public static final String VNPAY_QUERY_COMMAND = "querydr";
    public static final List<String> VNPAY_QUERY_RESPONSE_SIGNED_FIELDS = List.of("vnp_ResponseId", "vnp_Command",
            "vnp_ResponseCode", "vnp_Message", "vnp_TmnCode", "vnp_TxnRef", "vnp_Amount", "vnp_BankCode",
            "vnp_PayDate", "vnp_TransactionNo", "vnp_TransactionType", "vnp_TransactionStatus", "vnp_OrderInfo",
            "vnp_PromotionCode", "vnp_PromotionAmount");

    private Constant() {
    }
}
