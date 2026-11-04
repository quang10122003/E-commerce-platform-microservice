package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.port.in.CheckCheckoutAccessUseCase;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class InternalCheckoutController {

    private final CheckCheckoutAccessUseCase checkCheckoutAccessUseCase;

    @GetMapping("/checkouts/{checkoutId}/access")
    public ResponseEntity<Void> checkAccess(@PathVariable @Positive Long checkoutId) {
        checkCheckoutAccessUseCase.checkAccess(checkoutId);
        return ResponseEntity.noContent().build();
    }
}
