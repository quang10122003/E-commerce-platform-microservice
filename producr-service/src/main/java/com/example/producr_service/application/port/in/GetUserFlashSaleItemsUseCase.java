package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.UserFlashSaleResponse;

public interface GetUserFlashSaleItemsUseCase {
    // Lấy item của sale đang chạy, có thể lọc theo danh mục sản phẩm.
    UserFlashSaleResponse getItems(Long categoryId);
}
