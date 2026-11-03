package com.example.order_service.order.adapter.in.rest;

import com.example.order_service.order.application.dto.repone.CreateCheckoutResponse;
import com.example.order_service.order.application.dto.request.CreateCheckoutRequest;
import com.example.order_service.order.application.port.in.CreateCheckoutUseCase;
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
