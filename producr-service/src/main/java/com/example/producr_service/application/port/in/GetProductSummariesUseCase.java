package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductSummaryResponse;

import java.util.List;

public interface GetProductSummariesUseCase {
    List<ProductSummaryResponse> getProductSummariesByIds(List<Long> productIds);
}
