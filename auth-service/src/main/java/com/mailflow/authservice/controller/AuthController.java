package com.mailflow.authservice.controller;

import com.mailflow.authservice.dto.auth.LoginRequest;
import com.mailflow.authservice.dto.auth.TokenResponse;
import com.mailflow.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
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

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  public void logout(
      @RequestParam String userId, @RequestParam String refreshToken) {
    authService.logout(userId, refreshToken);
  }
}
