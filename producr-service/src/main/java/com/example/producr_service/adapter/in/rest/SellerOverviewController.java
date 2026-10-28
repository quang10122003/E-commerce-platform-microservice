package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.SellerProductOverviewResponse;
import com.example.producr_service.application.port.in.GetSellerProductOverviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/shop/overview")
@RequiredArgsConstructor
public class SellerOverviewController {
    private final GetSellerProductOverviewUseCase getSellerProductOverviewUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SHOP')")
    public ResponseEntity<SellerProductOverviewResponse> getProductOverview() {
        return ResponseEntity.ok(getSellerProductOverviewUseCase.getProductOverview());
    }
}
