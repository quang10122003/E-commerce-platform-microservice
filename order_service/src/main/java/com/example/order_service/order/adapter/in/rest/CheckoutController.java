package com.example.order_service.order.adapter.in.rest;

import com.example.order_service.order.application.dto.repone.CreateCheckoutResponse;
import com.example.order_service.order.application.dto.request.CreateCheckoutRequest;
import com.example.order_service.order.application.port.in.CreateCheckoutUseCase;
import com.example.order_service.order.application.port.in.CheckCheckoutAccessUseCase;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkouts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class CheckoutController {

    private final CreateCheckoutUseCase createCheckoutUseCase;
    private final CheckCheckoutAccessUseCase checkCheckoutAccessUseCase;

    @GetMapping("/{checkoutId}/access")
    public ResponseEntity<Void> checkAccess(@PathVariable @Positive Long checkoutId) {
        checkCheckoutAccessUseCase.checkAccess(checkoutId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<CreateCheckoutResponse> createCheckout(
            @Valid @RequestBody CreateCheckoutRequest request,
            @RequestHeader(name = "Idempotency-Key", defaultValue = "")
            @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
                    message = "Idempotency-Key phải là UUID") String idempotencyKey,
            HttpServletRequest servletRequest) {
        return ResponseEntity.accepted().body(createCheckoutUseCase.createCheckout(
                request, idempotencyKey, servletRequest.getRemoteAddr()));
    }
}
