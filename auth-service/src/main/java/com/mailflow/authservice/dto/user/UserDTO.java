package com.mailflow.authservice.dto.user;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record UserDTO(
        String id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        List<String> roles,
        Map<String, List<String>> attributes,
        Long createdTimestamp
) {}