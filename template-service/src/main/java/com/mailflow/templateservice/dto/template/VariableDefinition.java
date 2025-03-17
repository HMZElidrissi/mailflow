package com.mailflow.templateservice.dto.template;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VariableDefinition(
        @NotBlank(message = "Variable name is required") String name,
        String defaultValue,
        String description) {}