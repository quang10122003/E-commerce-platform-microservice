package com.example.auth_service.application.DTO.command;

public record OAuthLoginCommand(String email, boolean emailVerified, String providerId, String fullName) {
}
