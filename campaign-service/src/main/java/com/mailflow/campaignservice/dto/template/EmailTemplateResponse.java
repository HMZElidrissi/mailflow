package com.mailflow.campaignservice.dto.template;

import lombok.Builder;
import java.util.Map;

@Builder
public record EmailTemplateResponse(
    Long id,
    String name,
    String subject,
    String content,
    Map<String, String> variables,
    String description,
    String createdAt,
    String updatedAt) {}
