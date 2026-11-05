package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.response.SellerProductOverviewResponse;
import com.example.producr_service.application.port.in.GetSellerProductOverviewUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class SellerOverviewService implements GetSellerProductOverviewUseCase {
    private final CurrentUserPort currentUserPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ShopAccessService shopAccessService;

    @Override
    @Transactional(readOnly = true)
    public SellerProductOverviewResponse getProductOverview() {
        Long sellerUserId = shopAccessService.requireSellerAccess().userId();
        return productRepositoryPort.getSellerProductOverview(sellerUserId);
    }
}
