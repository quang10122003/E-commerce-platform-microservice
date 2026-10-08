package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ShopFlashSaleProductResponse;
import java.util.List;

public interface GetShopFlashSaleProductsUseCase {
    List<ShopFlashSaleProductResponse> getShopFlashSaleProducts();
}
