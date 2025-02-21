package com.mailflow.campaignservice.dto.campaign;

import lombok.Builder;

@Builder
public record CampaignActivatedEvent(Long campaignId, String triggerTag) {}
