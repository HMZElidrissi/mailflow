package com.mailflow.campaignservice.dto.template;

import java.util.Set;

public record EmailTemplateResponse(
        Long id,
        String name,
        String subject,
        String content,
        Set<String> variables,
        String description,
        String createdAt,
        String updatedAt
) {}