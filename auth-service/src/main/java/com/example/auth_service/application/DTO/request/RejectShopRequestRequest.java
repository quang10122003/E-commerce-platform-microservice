package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectShopRequestRequest(
        @NotBlank(message = "Vui lòng nhập lý do từ chối")
        @Size(max = 500, message = "Lý do từ chối tối đa 500 ký tự")
        String reason) {
}
