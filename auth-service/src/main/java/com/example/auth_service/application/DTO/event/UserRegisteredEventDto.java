package com.example.auth_service.application.DTO.event;

import jdk.dynalink.beans.StaticClass;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEventDto(
        UUID eventId,
        String eventType,
        Long userId,
        String email,
        String fullName,
        LocalDateTime occurredAt) {

    public final static String Event_Type = "USER_REGISTERED";
}
