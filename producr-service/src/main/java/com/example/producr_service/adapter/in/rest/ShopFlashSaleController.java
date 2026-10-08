package com.example.producr_service.adapter.in.rest;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopFlashSaleCampaignFilter;
import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignResponse;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.producr_service.application.port.in.RegisterFlashSaleItemUseCase;
import com.example.producr_service.application.port.in.PreviewFlashSalePriceUseCase;
import com.example.producr_service.application.dto.request.RegisterFlashSaleItemRequest;
import com.example.producr_service.application.dto.request.PreviewFlashSalePriceRequest;
import com.example.producr_service.application.dto.response.FlashSalePriceResponse;

@RestController
@RequestMapping("/api/shop/flash-sales")
@RequiredArgsConstructor
public class ShopFlashSaleController {
    private final GetShopFlashSaleCampaignsUseCase getShopFlashSaleCampaignsUseCase;
    private final RegisterFlashSaleItemUseCase registerFlashSaleItemUseCase;
    private final PreviewFlashSalePriceUseCase previewFlashSalePriceUseCase;
    // Đăng ký một variant vào chương trình, thành công không trả nội dung.
    @PostMapping("/{campaignId}/items")
    public ResponseEntity<Void> register(@PathVariable Long campaignId,
            @Valid @RequestBody RegisterFlashSaleItemRequest request) {
        registerFlashSaleItemUseCase.register(campaignId, request);
        return ResponseEntity.noContent().build();
    }

    // Trả giá sale dự kiến từ giá variant hiện tại và phần trăm Shop nhập.
    @PostMapping("/variants/{variantId}/preview-price")
    public ResponseEntity<FlashSalePriceResponse> previewPrice(@PathVariable Long variantId,
            @Valid @RequestBody PreviewFlashSalePriceRequest request) {
        return ResponseEntity.ok(previewFlashSalePriceUseCase.previewPrice(variantId, request));
    }

    // Đọc các chương trình DRAFT cho Shop, mặc định trang 1 và 20 bản ghi.
    @GetMapping
    public ResponseEntity<PageResponse<ShopFlashSaleCampaignResponse>> getCampaigns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Boolean registrationOpen) {
        return ResponseEntity.ok(getShopFlashSaleCampaignsUseCase.getShopCampaigns(
                new ShopFlashSaleCampaignFilter(page, size, from, to, registrationOpen)));
    }
}
