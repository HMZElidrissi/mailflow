package com.mailflow.campaignservice.dto.campaign;

import lombok.Builder;

@Builder
public record CampaignTriggeredEvent(
        Long campaignId,
        Long contactId,
        Long templateId
) {}