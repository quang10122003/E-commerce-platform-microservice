package com.example.auth_service.adapter.in.rest;

import com.example.auth_service.application.DTO.repone.ShopRequestResponse;
import com.example.auth_service.application.DTO.request.CreateShopRequestRequest;
import com.example.auth_service.application.port.in.CancelShopRequestUseCase;
import com.example.auth_service.application.port.in.CreateShopRequestUseCase;
import com.example.auth_service.application.port.in.GetMyShopRequestUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Đặt dưới /api/users/me để dùng lại route gateway sẵn có của auth-service.
@RestController
@RequestMapping("/api/users/me/shop-requests")
@RequiredArgsConstructor
@Validated
public class ShopRequestController {
    private final CreateShopRequestUseCase createShopRequestUseCase;
    private final GetMyShopRequestUseCase getMyShopRequestUseCase;
    private final CancelShopRequestUseCase cancelShopRequestUseCase;

    // Chưa từng nộp đơn thì data là null.
    @GetMapping("/latest")
    public ShopRequestResponse getMyLatestShopRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return getMyShopRequestUseCase.getMyLatestShopRequest(authorizationHeader).orElse(null);
    }

    @PostMapping
    public ResponseEntity<ShopRequestResponse> createShopRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                                 @Valid @RequestBody CreateShopRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createShopRequestUseCase.createShopRequest(authorizationHeader, request));
    }

    @PostMapping("/{requestId}/cancel")
    public ShopRequestResponse cancelShopRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                 @PathVariable @Positive Long requestId) {
        return cancelShopRequestUseCase.cancelShopRequest(authorizationHeader, requestId);
    }
}
