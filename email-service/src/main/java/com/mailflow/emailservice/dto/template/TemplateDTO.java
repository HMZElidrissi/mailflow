package com.mailflow.emailservice.dto.template;

import lombok.Builder;

import java.util.Map;

@Builder
public record TemplateDTO(
    Long id, String name, String subject, String content, Map<String, String> variables) {}
