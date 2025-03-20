package com.mailflow.emailservice.dto.contact;

import lombok.Builder;

@Builder
public record ContactDTO(Long id, String email, String firstName, String lastName, String[] tags) {}
