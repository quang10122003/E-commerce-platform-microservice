package com.example.common.flashsale;

// Product tạo bộ đếm suất còn lại, Order trừ và trả suất; hai service phải dùng cùng quy ước key.
public final class FlashSaleSlotKeys {

    private static final String PREFIX = "flash-sale:slot";

    private FlashSaleSlotKeys() {
    }

    public static String remaining(Long variantId) {
        return PREFIX + ":variant:" + variantId;
    }

    public static String reservation(String checkoutKey) {
        return PREFIX + ":reservation:" + checkoutKey;
    }
}
