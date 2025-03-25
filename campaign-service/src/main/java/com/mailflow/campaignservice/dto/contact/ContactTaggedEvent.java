package com.mailflow.campaignservice.dto.contact;

import lombok.Builder;

@Builder
public record ContactTaggedEvent(Long contactId, String contactEmail, String tag) {}
