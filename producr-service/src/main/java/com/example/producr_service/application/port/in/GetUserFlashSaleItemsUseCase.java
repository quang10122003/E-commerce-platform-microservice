package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.UserFlashSaleResponse;

public interface GetUserFlashSaleItemsUseCase {
    UserFlashSaleResponse getItems(Long categoryId);

    UserFlashSaleResponse getTopSellingItems();
}
