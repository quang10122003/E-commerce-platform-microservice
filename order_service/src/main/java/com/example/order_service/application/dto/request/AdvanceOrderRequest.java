package com.example.order_service.application.dto.request;

import com.example.order_service.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

// Trạng thái đơn mà UI đang thấy; BE tự chọn bước kế tiếp.
public record AdvanceOrderRequest(@NotNull(message = "Thiếu trạng thái hiện tại của đơn") OrderStatus currentStatus) {
}
