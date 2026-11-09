package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.port.in.GetCurrentFlashSaleCampaignUseCase;
import com.example.producr_service.application.port.in.GetAdminFlashSaleCampaignDetailUseCase;
import com.example.producr_service.application.port.in.UpdateFlashSaleItemStatusUseCase;
import com.example.producr_service.application.dto.request.UpdateFlashSaleItemStatusRequest;
import com.example.producr_service.application.dto.response.CurrentFlashSaleCampaignResponse;
import com.example.producr_service.application.dto.response.AdminFlashSaleCampaignDetailResponse;
import com.example.producr_service.domain.model.FlashSaleItemStatus;

import com.example.producr_service.application.dto.request.CreateFlashSaleCampaignRequest;
import com.example.producr_service.application.port.in.CreateFlashSaleCampaignUseCase;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.FlashSaleCampaignResponse;
import com.example.producr_service.application.port.in.GetFlashSaleCampaignsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import com.example.producr_service.application.dto.request.FlashSaleCampaignFilter;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import com.example.producr_service.application.port.in.CancelFlashSaleCampaignUseCase;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/flash-sales")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminFlashSaleController {
    private final CreateFlashSaleCampaignUseCase createFlashSaleCampaignUseCase;
    private final GetFlashSaleCampaignsUseCase getFlashSaleCampaignsUseCase;

    private final CancelFlashSaleCampaignUseCase cancelFlashSaleCampaignUseCase;

    private final GetCurrentFlashSaleCampaignUseCase getCurrentFlashSaleCampaignUseCase;
    private final UpdateFlashSaleItemStatusUseCase updateFlashSaleItemStatusUseCase;
    private final GetAdminFlashSaleCampaignDetailUseCase getAdminFlashSaleCampaignDetailUseCase;

    @PostMapping("/{campaignId}/items/{itemId}/status")
    public ResponseEntity<Void> updateItemStatus(@PathVariable Long campaignId, @PathVariable Long itemId,
            @Valid @RequestBody UpdateFlashSaleItemStatusRequest request) {
        updateFlashSaleItemStatusUseCase.updateItemStatus(campaignId, itemId, request);
        return ResponseEntity.noContent().build();
    }

    // Hủy lại chương trình đã hủy vẫn trả 204 để request lặp không trở thành lỗi.
    @PutMapping("/{campaignId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long campaignId) {
        cancelFlashSaleCampaignUseCase.cancel(campaignId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{campaignId}")
    public ResponseEntity<AdminFlashSaleCampaignDetailResponse> getCampaignDetail(
            @PathVariable Long campaignId, @RequestParam(required = false) FlashSaleItemStatus status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(getAdminFlashSaleCampaignDetailUseCase.getCampaignDetail(campaignId, status, keyword));
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentFlashSaleCampaignResponse> getCurrentCampaign(
            @RequestParam(required = false) FlashSaleItemStatus status,
            @RequestParam(required = false) String keyword) {
        return getCurrentFlashSaleCampaignUseCase.getCurrentCampaign(status, keyword)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<PageResponse<FlashSaleCampaignResponse>> getCampaigns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) FlashSaleCampaignStatus status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(getFlashSaleCampaignsUseCase.getCampaigns(
                new FlashSaleCampaignFilter(page, size, from, to, status, keyword)));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateFlashSaleCampaignRequest request) {
        createFlashSaleCampaignUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
