package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.port.in.CheckUserActiveOrdersUseCase;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Auth-service gọi kèm token admin để quyết định có cho xóa tài khoản hay không.
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class InternalUserOrderController {
    private final CheckUserActiveOrdersUseCase checkUserActiveOrdersUseCase;

    @GetMapping("/{userId}/active-orders")
    public boolean hasActiveOrders(@PathVariable @Positive Long userId) {
        return checkUserActiveOrdersUseCase.hasActiveOrders(userId);
    }
}
