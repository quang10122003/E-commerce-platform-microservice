package com.example.notification.application.port.out.client;

import com.example.notification.application.dto.response.ChatProductInfo;

import java.util.List;

public interface ProductClientPort {
    // Tra cứu tên và ảnh sản phẩm theo lô, bỏ qua sản phẩm đã bị xóa.
    List<ChatProductInfo> findProductInfosByIds(List<Long> productIds);

    // Kiểm tra sản phẩm còn tồn tại, kể cả khi không còn ACTIVE.
    boolean existsById(Long productId);

    // Lấy ID shop sở hữu sản phẩm, không che lỗi khi sản phẩm không tồn tại.
    Long getShopId(Long productId);
}
