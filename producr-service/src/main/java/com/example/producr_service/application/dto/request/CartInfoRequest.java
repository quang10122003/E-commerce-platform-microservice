package com.example.producr_service.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CartInfoRequest(
        @NotEmpty(message = "Danh sách variant không được để trống")
        List<@NotNull(message = "ID variant không được để trống")
                @Positive(message = "ID variant phải lớn hơn 0") Long> variantIds
) {
}
