package com.mailflow.emailservice.dto.campaign;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CampaignTriggeredEvent(
    Long campaignId, Long contactId, Long templateId, String eventId, LocalDateTime timestamp) {}
