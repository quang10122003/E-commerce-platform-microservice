package com.example.order_service.cart.application.Constant;

public final class Constant {
    public static final String ORDER_EVENTS_TOPIC = "order-service.events";
    public static final String CART = "Cart";
    public static final int OUTBOX_BATCH_SIZE = 100;
    public static final int OUTBOX_PUBLISH_TIMEOUT_SECONDS = 20;
    public static final int ORDER_EVENTS_PARTITIONS = 3;
    public static final int ORDER_EVENTS_REPLICAS = 1;
    public static final String PRODUCT_EVENTS_TOPIC = "product-service.events";
    public static final String DEAD_LETTER_TOPIC = "order-service.DLT";
}
