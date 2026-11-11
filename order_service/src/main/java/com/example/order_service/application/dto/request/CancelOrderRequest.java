package com.example.order_service.application.dto.request;

import com.example.order_service.domain.model.OrderCancelReason;
import jakarta.validation.constraints.NotNull;

public record CancelOrderRequest(@NotNull(message = "Vui lòng chọn lý do hủy") OrderCancelReason reason) {
}
