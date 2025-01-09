package com.mailflow.auth.controller;

import com.mailflow.auth.dto.LoginRequest;
import com.mailflow.auth.dto.RegisterRequest;
import com.mailflow.auth.service.KeycloakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakService keycloakService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return keycloakService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return keycloakService.login(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email) {
        return keycloakService.initiatePasswordReset(email);
    }
}