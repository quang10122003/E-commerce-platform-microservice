package com.example.producr_service.application.dto.response;

import java.util.List;

public record ShopFlashSaleProductResponse(
        Long id,
        String name,
        String categoryName,
        SellerProductItemResponse.DisplayStatus status,
        List<SellerProductItemResponse.Variant> variants
) {}
