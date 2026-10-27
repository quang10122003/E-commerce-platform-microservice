package com.example.producr_service.application.constant;

public final class ProductCacheKeys {
    private static final String PREFIX = "product";

    public static final String TOP_SELLING_PRODUCTS = PREFIX + ":top-selling";

    public static String recommendations(Long userId) {
        return PREFIX + ":recommendations:" + userId;
    }

    private ProductCacheKeys() {
    }
}
