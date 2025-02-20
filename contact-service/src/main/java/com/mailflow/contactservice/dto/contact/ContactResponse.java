package com.mailflow.contactservice.dto.contact;

import lombok.Builder;

import java.util.Set;

@Builder
public record ContactResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    Set<String> tags,
    String createdAt,
    String updatedAt) {}
