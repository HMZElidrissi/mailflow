package com.mailflow.templateservice.dto.template;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

@Builder
public record EmailTemplateRequest(
        @NotBlank(message = "Template name is required") String name,
        @NotBlank(message = "Subject is required") String subject,
        @NotBlank(message = "Content is required") String content,
        Map<String, String> variables,
        String description) {}