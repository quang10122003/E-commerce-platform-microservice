package com.example.producr_service.application.dto.request;

import com.example.producr_service.domain.model.FlashSaleItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFlashSaleItemStatusRequest(
        @NotNull(message = "Trạng thái đăng ký không được để trống") FlashSaleItemStatus status) {}
