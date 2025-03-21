package com.mailflow.authservice.controller;

import com.mailflow.authservice.dto.auth.LoginRequest;
import com.mailflow.authservice.dto.auth.TokenResponse;
import com.mailflow.authservice.dto.error.ErrorResponse;
import com.mailflow.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest.email(), loginRequest.password());
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @GetMapping("/validate")
    public boolean validateToken(@RequestParam String token) {
        return authService.validateToken(token);
    }

    @GetMapping("/test")
    public ErrorResponse test() {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .error(HttpStatus.OK.getReasonPhrase())
                .message("Test endpoint")
                .path("/api/auth/test")
                .build();
    }
}