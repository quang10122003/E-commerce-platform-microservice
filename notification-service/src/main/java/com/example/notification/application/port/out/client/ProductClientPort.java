package com.example.notification.application.port.out.client;

public interface ProductClientPort {

    // Kiểm tra sản phẩm còn tồn tại, kể cả khi không còn ACTIVE.
    boolean existsById(Long productId);

    // Lấy ID shop sở hữu sản phẩm, không che lỗi khi sản phẩm không tồn tại.
    Long getShopId(Long productId);
}
