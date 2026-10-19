package com.example.producr_service.adapter.in.rest;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopFlashSaleCampaignFilter;
import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignResponse;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;
import java.util.List;
import com.example.producr_service.application.port.in.GetShopFlashSaleCampaignDetailUseCase;
import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignDetailResponse;
import com.example.producr_service.application.port.in.GetShopFlashSaleProductsUseCase;
import com.example.producr_service.application.dto.response.ShopFlashSaleProductResponse;
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
@Slf4j
public class ShopFlashSaleController {
    private final GetShopFlashSaleCampaignsUseCase getShopFlashSaleCampaignsUseCase;
    private final RegisterFlashSaleItemUseCase registerFlashSaleItemUseCase;
    private final PreviewFlashSalePriceUseCase previewFlashSalePriceUseCase;
    private final GetShopFlashSaleProductsUseCase getShopFlashSaleProductsUseCase;

    private final GetShopFlashSaleCampaignDetailUseCase getShopFlashSaleCampaignDetailUseCase;

    @GetMapping("/{campaignId}")
    public ResponseEntity<ShopFlashSaleCampaignDetailResponse> getCampaignDetail(@PathVariable Long campaignId) {
        return ResponseEntity.ok(getShopFlashSaleCampaignDetailUseCase.getShopCampaignDetail(campaignId));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ShopFlashSaleProductResponse>> getProducts() {
        return ResponseEntity.ok(getShopFlashSaleProductsUseCase.getShopFlashSaleProducts());
    }

    @PostMapping("/{campaignId}/items")
    public ResponseEntity<Void> register(@PathVariable Long campaignId,
            @Valid @RequestBody RegisterFlashSaleItemRequest request) {
        registerFlashSaleItemUseCase.register(campaignId, request);
        log.info("Đăng ký variant vào Flash Sale thành công: campaignId={}, variantId={}",
                campaignId, request.variantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/variants/{variantId}/preview-price")
    public ResponseEntity<FlashSalePriceResponse> previewPrice(@PathVariable Long variantId,
            @Valid @RequestBody PreviewFlashSalePriceRequest request) {
        return ResponseEntity.ok(previewFlashSalePriceUseCase.previewPrice(variantId, request));
    }

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
