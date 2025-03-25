package com.mailflow.contactservice.dto.contact;

import lombok.Builder;

@Builder
public record ContactTaggedEvent(Long contactId, String contactEmail, String tag) {}
