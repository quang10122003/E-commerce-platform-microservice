package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.dto.request.CancelOrderRequest;
import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.application.port.in.CancelMyOrderUseCase;
import com.example.order_service.application.port.in.GetMyOrdersUseCase;
import com.example.order_service.domain.model.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import com.example.common.response.PageResponse;
import com.example.common.response.PageQuery;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class OrderController {

    private final GetMyOrdersUseCase getMyOrdersUseCase;
    private final CancelMyOrderUseCase cancelMyOrderUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<UserOrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(getMyOrdersUseCase.getMyOrders(status, keyword,
                PageQuery.builder().page(page).size(size).build()));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelMyOrder(@PathVariable Long orderId, @Valid @RequestBody CancelOrderRequest request) {
        cancelMyOrderUseCase.cancelMyOrder(orderId, request.reason());
        return ResponseEntity.noContent().build();
    }
}
