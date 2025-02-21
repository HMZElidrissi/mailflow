package com.mailflow.campaignservice.dto.campaign;

import lombok.*;

@Builder
public record CampaignResponse(
    Long id,
    String name,
    String triggerTag,
    Long templateId,
    boolean active,
    String createdAt,
    String updatedAt) {}
