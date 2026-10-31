package com.example.producr_service.application.dto.request;

import com.example.producr_service.domain.model.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(@NotNull ProductStatus status) {
}
