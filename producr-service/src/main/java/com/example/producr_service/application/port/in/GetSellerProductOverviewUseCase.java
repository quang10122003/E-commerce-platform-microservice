package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.SellerProductOverviewResponse;

public interface GetSellerProductOverviewUseCase {
    SellerProductOverviewResponse getProductOverview();
}
