package com.example.notification.application.port.out.client;

import com.example.notification.application.dto.response.ChatProductInfo;

import java.util.List;

public interface ProductClientPort {
    // Sản phẩm đã xóa được bỏ qua, không làm hỏng danh sách chat.
    List<ChatProductInfo> findProductInfosByIds(List<Long> productIds);

    // Sản phẩm ngừng bán vẫn được coi là tồn tại để không mất liên kết chat.
    boolean existsById(Long productId);

    // Không nuốt lỗi sản phẩm không tồn tại khi xác định chủ shop.
    Long getShopId(Long productId);
}
