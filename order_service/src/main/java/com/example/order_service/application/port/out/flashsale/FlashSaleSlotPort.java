package com.example.order_service.application.port.out.flashsale;

import java.util.Map;

public interface FlashSaleSlotPort {
    // Trả false khi một biến thể sale đã hết suất; biến thể chưa có bộ đếm được cho qua để saga kiểm tra bằng DB.
    boolean reserve(String checkoutKey, Map<Long, Integer> quantitiesByVariant);

    // Trả lại đúng một lần các suất đã giữ cho checkout; gọi lại nhiều lần không cộng dư.
    void release(String checkoutKey);
}
