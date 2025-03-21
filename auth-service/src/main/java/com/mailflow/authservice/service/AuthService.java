package com.mailflow.authservice.service;

import com.mailflow.authservice.dto.auth.TokenResponse;

public interface AuthService {

    TokenResponse login(String email, String password);

    TokenResponse refreshToken(String refreshToken);

    boolean validateToken(String token);

    void logout(String userId, String refreshToken);
}