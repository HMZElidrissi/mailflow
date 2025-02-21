package com.mailflow.campaignservice.dto.campaign;

import lombok.Builder;

@Builder
public record ContactTaggedEvent(Long contactId, String email, String tag) {}
