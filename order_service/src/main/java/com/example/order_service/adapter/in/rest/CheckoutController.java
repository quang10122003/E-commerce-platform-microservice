package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.dto.response.CreateCheckoutResponse;
import com.example.order_service.application.dto.request.CreateCheckoutRequest;
import com.example.order_service.application.port.in.CreateCheckoutUseCase;
import com.example.order_service.application.port.in.GetCheckoutUseCase;
import com.example.order_service.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.application.dto.response.CheckoutQuoteResponse;
import com.example.order_service.application.port.in.GetCheckoutQuoteUseCase;
import com.example.order_service.application.port.in.GetCheckoutItemsUseCase;
import com.example.order_service.application.dto.request.CheckoutItemsRequest;
import com.example.order_service.application.dto.response.CheckoutItemsResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkouts")
@RequiredArgsConstructor
public class CheckoutController {

    private final CreateCheckoutUseCase createCheckoutUseCase;
    private final GetCheckoutItemsUseCase getCheckoutItemsUseCase;
    private final GetCheckoutQuoteUseCase getCheckoutQuoteUseCase;
    private final GetCheckoutUseCase getCheckoutUseCase;

    @GetMapping("/{checkoutId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<CreateCheckoutResponse> getCheckout(@PathVariable @Positive Long checkoutId) {
        return ResponseEntity.ok(getCheckoutUseCase.getCheckout(checkoutId));
    }

    @PostMapping("/quote")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<CheckoutQuoteResponse> getCheckoutQuote(@Valid @RequestBody CheckoutQuoteRequest request) {
        return ResponseEntity.ok(getCheckoutQuoteUseCase.getCheckoutQuote(request));
    }

    @PostMapping("/items")
    public ResponseEntity<CheckoutItemsResponse> getCheckoutItems(@Valid @RequestBody CheckoutItemsRequest request) {
        return ResponseEntity.ok(getCheckoutItemsUseCase.getCheckoutItems(request));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
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
