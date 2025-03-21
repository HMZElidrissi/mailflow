package com.mailflow.authservice.dto.user;

import jakarta.validation.constraints.Email;

import java.util.List;
import java.util.Map;

public record UserUpdateRequest(
        @Email(message = "Email must be valid") String email,
        String firstName,
        String lastName,
        Boolean enabled,
        List<String> roles,
        Map<String, List<String>> attributes
) {}