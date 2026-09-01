package com.example.auth_service.application.DTO.repone;


import com.example.auth_service.domain.model.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthenticatedUser {
    User user;
}
