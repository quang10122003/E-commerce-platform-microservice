package com.example.auth_service.domain.until;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeToken {
    ACCESS_TOKEN("access"),
    REFRESH_TOKEN("refresh");
    private  final  String name;
}
