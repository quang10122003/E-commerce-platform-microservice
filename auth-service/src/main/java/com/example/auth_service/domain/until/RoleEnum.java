package com.example.auth_service.domain.until;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleEnum {
    User("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    SHOP("ROLE_SHOP");
    private final String name;

}
