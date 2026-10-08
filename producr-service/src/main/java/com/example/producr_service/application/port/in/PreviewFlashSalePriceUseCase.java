package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.PreviewFlashSalePriceRequest;
import com.example.producr_service.application.dto.response.FlashSalePriceResponse;

public interface PreviewFlashSalePriceUseCase {
    FlashSalePriceResponse previewPrice(Long variantId, PreviewFlashSalePriceRequest request);
}
