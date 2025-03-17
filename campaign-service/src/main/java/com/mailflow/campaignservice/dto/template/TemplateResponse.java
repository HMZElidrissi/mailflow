package com.mailflow.campaignservice.dto.template;

import java.util.Set;

public record TemplateResponse(
        Long id,
        String name,
        String subject,
        String content,
        Set<String> variables,
        String createdAt,
        String updatedAt
) {}