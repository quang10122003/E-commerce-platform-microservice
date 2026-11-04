package com.example.order_service.application.dto.request;

import com.example.order_service.application.dto.CheckoutItemDto;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateCheckoutRequest(
        @NotEmpty List<@NotNull @Valid CheckoutItemDto> items,
        @NotNull CheckoutPaymentMethod paymentMethod,
        @NotBlank @Size(max = 1000) String shippingAddress,
        @NotNull @DecimalMin("1") @Digits(integer = 19, fraction = 0) BigDecimal totalAmount,
        @Size(max = 1000) String note) {
}
