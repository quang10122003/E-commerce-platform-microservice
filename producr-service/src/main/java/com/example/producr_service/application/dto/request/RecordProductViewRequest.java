package com.example.producr_service.application.dto.request;

import com.example.producr_service.application.constant.Constant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecordProductViewRequest(
        @NotBlank
        @Pattern(regexp = Constant.BEHAVIOR_EVENT_ID_PATTERN)
        String eventId
) {
}
