package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.dto.response.AdminAnalyticsResponse;
import com.example.order_service.application.port.in.GetAdminAnalyticsUseCase;
import com.example.order_service.application.port.in.GetAdminOverviewUseCase;
import com.example.order_service.application.dto.response.AdminOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminOrderController {

    private final GetAdminAnalyticsUseCase getAdminAnalyticsUseCase;
    private final GetAdminOverviewUseCase getAdminOverviewUseCase;

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> getAdminOverview() {
        return ResponseEntity.ok(getAdminOverviewUseCase.getAdminOverview());
    }

    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsResponse> getAdminAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(getAdminAnalyticsUseCase.getAdminAnalytics(from, to));
    }
}
