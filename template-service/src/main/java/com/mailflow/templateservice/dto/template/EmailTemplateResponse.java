package com.mailflow.templateservice.dto.template;

import java.util.Map;
import lombok.Builder;

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
