package com.mailflow.campaignservice.dto.contact;

import java.util.Set;

public record ContactResponse(
    Long id,
    String email,
    String firsName,
    String lastName,
    Set<String> tags,
    String createdAt,
    String updatedAt) {}
