package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.command.UpdateProductCommand;
import com.example.producr_service.application.dto.response.ShopProductDetailResponse;

// Lưu thông tin sản phẩm và các phân loại trong cùng một lần cập nhật.
public interface UpdateProductUseCase {
    ShopProductDetailResponse updateProduct(Long productId, UpdateProductCommand command);
}
