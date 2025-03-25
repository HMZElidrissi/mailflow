package com.mailflow.authservice.dto.auth;

public record LogoutRequest(String userId, String refreshToken) {}
