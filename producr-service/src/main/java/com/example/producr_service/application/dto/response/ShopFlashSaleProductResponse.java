package com.example.producr_service.application.dto.response;

import java.util.List;

// Dữ liệu chọn sản phẩm và phân loại trong biểu mẫu đăng ký Flash Sale.
public record ShopFlashSaleProductResponse(
        Long id,
        String name,
        String categoryName,
        SellerProductItemResponse.DisplayStatus status,
        List<SellerProductItemResponse.Variant> variants
) {}
