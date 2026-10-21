package com.example.producr_service.adapter.DTO.request;

import com.example.producr_service.application.dto.request.UpdateShopFlashSaleItemRequest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateShopFlashSaleItemHttpRequest(
        // Giữ nguyên số từ JSON để validation không bỏ sót phần thập phân bị cắt ngầm.
        @NotNull @Min(1) @Max(Integer.MAX_VALUE) @Digits(integer = 10, fraction = 0)
        BigDecimal registeredQuantity,
        // Null hoặc không truyền nghĩa là giữ nguyên % giảm giá đã đăng ký.
        @DecimalMin(value = "0", inclusive = false)
        @DecimalMax(value = "100", inclusive = false) @Digits(integer = 2, fraction = 2)
        BigDecimal discountPercent) {

    public UpdateShopFlashSaleItemRequest toApplicationRequest() {
        return new UpdateShopFlashSaleItemRequest(registeredQuantity.intValueExact(), discountPercent);
    }
}
