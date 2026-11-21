package com.example.auth_service.adapter.in.rest;

import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;
import com.example.auth_service.application.DTO.request.RejectShopRequestRequest;
import com.example.auth_service.application.port.in.ApproveShopRequestUseCase;
import com.example.auth_service.application.port.in.GetShopRequestsUseCase;
import com.example.auth_service.application.port.in.RejectShopRequestUseCase;
import com.example.auth_service.domain.model.ShopRequestStatus;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Đặt dưới /api/users để dùng lại route gateway sẵn có của auth-service.
@RestController
@RequestMapping("/api/users/shop-requests")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminShopRequestController {
    private final GetShopRequestsUseCase getShopRequestsUseCase;
    private final ApproveShopRequestUseCase approveShopRequestUseCase;
    private final RejectShopRequestUseCase rejectShopRequestUseCase;

    @GetMapping
    public PageResponse<AdminShopRequestResponse> getShopRequests(
            @RequestParam(required = false) ShopRequestStatus status,
            @RequestParam(required = false) @Size(max = 255) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getShopRequestsUseCase.getShopRequests(status, keyword, new PageQuery(page, size));
    }

    @PostMapping("/{requestId}/approve")
    public AdminShopRequestResponse approveShopRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                       @PathVariable @Positive Long requestId) {
        return approveShopRequestUseCase.approveShopRequest(authorizationHeader, requestId);
    }

    @PostMapping("/{requestId}/reject")
    public AdminShopRequestResponse rejectShopRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                      @PathVariable @Positive Long requestId,
                                                      @Valid @RequestBody RejectShopRequestRequest request) {
        return rejectShopRequestUseCase.rejectShopRequest(authorizationHeader, requestId, request.reason());
    }
}
