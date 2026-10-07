package com.example.producr_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateFlashSaleCampaignRequest(
        @NotBlank(message = "Tên chương trình không được để trống")
        @Size(max = 255, message = "Tên chương trình không được vượt 255 ký tự")
        String name,

        String description,

        @NotNull(message = "Hạn đăng ký không được để trống")
        OffsetDateTime registrationDeadline,

        @NotNull(message = "Thời điểm bắt đầu không được để trống")
        OffsetDateTime startsAt,

        @NotNull(message = "Thời điểm kết thúc không được để trống")
        OffsetDateTime endsAt
) {
}
