package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.dto.request.AdvanceOrderRequest;
import com.example.order_service.application.dto.request.CancelOrderRequest;
import com.example.order_service.application.dto.request.ShopOrderFilter;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse;
import com.example.order_service.application.dto.response.ShopOrderResponse;
import com.example.order_service.application.port.in.AdvanceShopOrderUseCase;
import com.example.order_service.application.port.in.CancelShopOrderUseCase;
import com.example.order_service.application.port.in.GetShopAnalyticsUseCase;
import com.example.order_service.application.port.in.GetShopOverviewUseCase;
import com.example.order_service.application.dto.response.ShopOverviewResponse;
import com.example.order_service.application.port.in.GetShopOrdersUseCase;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders/shop")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('ROLE_SHOP')")
public class ShopOrderController {

    private final GetShopOrdersUseCase getShopOrdersUseCase;
    private final CancelShopOrderUseCase cancelShopOrderUseCase;
    private final AdvanceShopOrderUseCase advanceShopOrderUseCase;
    private final GetShopAnalyticsUseCase getShopAnalyticsUseCase;
    private final GetShopOverviewUseCase getShopOverviewUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ShopOverviewResponse> getShopOverview() {
        return ResponseEntity.ok(getShopOverviewUseCase.getShopOverview());
    }

    @GetMapping
    public ResponseEntity<List<ShopOrderResponse>> getShopOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) CheckoutPaymentMethod paymentMethod,
            @RequestParam(required = false) @Size(max = 100) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(getShopOrdersUseCase.getShopOrders(new ShopOrderFilter(status, paymentMethod, keyword, from, to)));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ShopAnalyticsResponse> getShopAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(getShopAnalyticsUseCase.getShopAnalytics(from, to));
    }

    @PostMapping("/{orderId}/advance")
    public ResponseEntity<Void> advanceShopOrder(@PathVariable Long orderId, @Valid @RequestBody AdvanceOrderRequest request) {
        advanceShopOrderUseCase.advanceShopOrder(orderId, request.currentStatus());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelShopOrder(@PathVariable Long orderId, @Valid @RequestBody CancelOrderRequest request) {
        cancelShopOrderUseCase.cancelShopOrder(orderId, request.reason());
        return ResponseEntity.noContent().build();
    }
}
