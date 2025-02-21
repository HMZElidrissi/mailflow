package com.mailflow.campaignservice.dto.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CampaignRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Trigger tag is required") String triggerTag,
    @NotNull(message = "Template ID is required") Long templateId) {}
