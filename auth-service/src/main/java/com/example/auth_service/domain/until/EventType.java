package com.example.auth_service.domain.until;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventType {

    USER_REGISTERED("UserRegistered");

    private final String value;
}
