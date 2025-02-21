package com.mailflow.campaignservice.dto.campaign;

import lombok.Builder;

@Builder
public record CampaignCreatedEvent(Long campaignId, String name, String triggerTag) {}
